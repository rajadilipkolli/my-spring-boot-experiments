package com.example.mongoes.config;

import com.example.mongoes.document.ChangeStreamResume;
import com.example.mongoes.document.Restaurant;
import com.example.mongoes.repository.elasticsearch.RestaurantESRepository;
import com.example.mongoes.repository.mongodb.ChangeStreamResumeRepository;
import com.example.mongoes.utils.AppConstants;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.bson.BsonTimestamp;
import org.bson.BsonValue;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

@Component
public class ChangeStreamStartupListener {

    private static final Logger log = LoggerFactory.getLogger(ChangeStreamStartupListener.class);
    private static final int BATCH_SIZE = 50;
    private static final Duration BATCH_TIMEOUT = Duration.ofMillis(100);

    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final RestaurantESRepository restaurantESRepository;
    private final ChangeStreamResumeRepository changeStreamResumeRepository;

    public ChangeStreamStartupListener(
            ReactiveMongoTemplate reactiveMongoTemplate,
            RestaurantESRepository restaurantESRepository,
            ChangeStreamResumeRepository changeStreamResumeRepository) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
        this.restaurantESRepository = restaurantESRepository;
        this.changeStreamResumeRepository = changeStreamResumeRepository;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void startListeningToChangeStream() {
        log.info("Initializing MongoDB change stream listener");
        changeStreamProcessor()
                .log()
                .doOnError(error -> log.error("Error in change stream: {}", error.getMessage()))
                .doOnComplete(() -> log.info("Change stream completed"))
                .retryWhen(
                        Retry.backoff(3, Duration.ofSeconds(10))
                                .maxBackoff(Duration.ofSeconds(10))
                                .doBeforeRetry(
                                        signal ->
                                                log.warn(
                                                        "Retrying after failure: {}. Attempt: {}",
                                                        signal.failure().getMessage(),
                                                        signal.totalRetries() + 1)))
                .subscribe();
    }

    public Flux<ChangeStreamEvent<Restaurant>> changeStreamProcessor() {
        return getChangeStreamOption()
                .flatMapMany(
                        options ->
                                reactiveMongoTemplate
                                        .changeStream(Restaurant.class)
                                        .watchCollection(AppConstants.RESTAURANT_COLLECTION)
                                        .resumeAt(options)
                                        .listen())
                .publishOn(Schedulers.boundedElastic())
                .bufferTimeout(BATCH_SIZE, BATCH_TIMEOUT, true)
                .filter(batch -> !batch.isEmpty())
                .concatMap(this::processBatch);
    }

    // Visible for testing
    Flux<ChangeStreamEvent<Restaurant>> processBatch(List<ChangeStreamEvent<Restaurant>> batch) {
        List<Restaurant> toSave = new ArrayList<>();
        List<String> idsToDelete = new ArrayList<>();

        for (ChangeStreamEvent<Restaurant> event : batch) {
            log.info("Processed change stream event at {}", event.getTimestamp());
            OperationType operationType = event.getOperationType();
            Restaurant restaurant = event.getBody();

            if (operationType == OperationType.DELETE) {
                String id =
                        (restaurant != null && restaurant.getId() != null)
                                ? restaurant.getId()
                                : extractId(event.getRaw());
                if (id != null) {
                    toSave.removeIf(r -> id.equals(r.getId()));
                    if (!idsToDelete.contains(id)) {
                        idsToDelete.add(id);
                    }
                } else {
                    log.warn(
                            "Document key is null for DELETE operation. Cannot delete restaurant"
                                    + " from repository: {}",
                            event);
                }
            } else if (operationType == OperationType.INSERT
                    || operationType == OperationType.REPLACE
                    || operationType == OperationType.UPDATE) {
                if (restaurant != null) {
                    if (restaurant.getId() != null) {
                        idsToDelete.remove(restaurant.getId());
                        toSave.removeIf(r -> restaurant.getId().equals(r.getId()));
                    }
                    toSave.add(restaurant);
                } else {
                    log.warn("Received {} event without body: {}", operationType, event);
                }
            }
        }

        Mono<Void> saveOp =
                toSave.isEmpty()
                        ? Mono.empty()
                        : this.restaurantESRepository
                                .saveAll(toSave)
                                .doOnError(
                                        error ->
                                                log.error(
                                                        "Error bulk saving restaurants: {}",
                                                        error.getMessage()))
                                .then();

        Mono<Void> deleteOp =
                idsToDelete.isEmpty()
                        ? Mono.empty()
                        : this.restaurantESRepository
                                .deleteAllById(idsToDelete)
                                .doOnError(
                                        error ->
                                                log.error(
                                                        "Error bulk deleting restaurants: {}",
                                                        error.getMessage()));

        BsonTimestamp latestTimestamp = getLatestTimestamp(batch);
        Mono<Void> resumeTokenOp =
                latestTimestamp == null
                        ? Mono.empty()
                        : this.changeStreamResumeRepository
                                .update(latestTimestamp)
                                .retryWhen(
                                        Retry.backoff(3, Duration.ofSeconds(1))
                                                .maxBackoff(Duration.ofSeconds(10)))
                                .doOnError(
                                        error ->
                                                log.error(
                                                        "Failed to update resume token: {}",
                                                        error.getMessage()))
                                .then();

        return saveOp.then(deleteOp).then(resumeTokenOp).thenMany(Flux.fromIterable(batch));
    }

    private BsonTimestamp getLatestTimestamp(List<ChangeStreamEvent<Restaurant>> batch) {
        ChangeStreamEvent<Restaurant> lastEvent = batch.getLast();
        if (lastEvent.getBsonTimestamp() != null) {
            return lastEvent.getBsonTimestamp();
        }
        if (lastEvent.getRaw() != null) {
            return lastEvent.getRaw().getClusterTime();
        }
        return null;
    }

    private String extractId(ChangeStreamDocument<Document> eventRaw) {
        if (eventRaw == null || eventRaw.getDocumentKey() == null) {
            return null;
        }
        BsonValue idValue = eventRaw.getDocumentKey().get("_id");
        if (idValue == null) {
            return null;
        }
        if (idValue.isObjectId()) {
            return idValue.asObjectId().getValue().toHexString();
        } else if (idValue.isString()) {
            return idValue.asString().getValue();
        } else {
            return idValue.toString();
        }
    }

    private Mono<ChangeStreamOptions> getChangeStreamOption() {
        return getResumeToken()
                .map(
                        resumeToken -> {
                            ChangeStreamOptions.ChangeStreamOptionsBuilder
                                    changeStreamOptionsBuilder =
                                            ChangeStreamOptions.builder()
                                                    .resumeAt(resumeToken.getResumeTimestamp());
                            changeStreamOptionsBuilder.returnFullDocumentOnUpdate();
                            return changeStreamOptionsBuilder.build();
                        })
                .defaultIfEmpty(
                        ChangeStreamOptions.builder()
                                .resumeAt(new BsonTimestamp(Instant.now().getEpochSecond()))
                                .returnFullDocumentOnUpdate()
                                .build());
    }

    private Mono<ChangeStreamResume> getResumeToken() {
        return this.changeStreamResumeRepository.findFirstByOrderByResumeTimestampDesc();
    }
}
