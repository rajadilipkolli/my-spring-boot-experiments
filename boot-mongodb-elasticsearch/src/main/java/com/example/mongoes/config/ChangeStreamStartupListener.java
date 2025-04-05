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
import java.util.Objects;
import org.bson.BsonTimestamp;
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
                .delayElements(Duration.ofMillis(1))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(
                        restaurantChangeStreamEvent -> {
                            log.info(
                                    "processed at {} ", restaurantChangeStreamEvent.getTimestamp());
                            Restaurant restaurant = restaurantChangeStreamEvent.getBody();
                            if (restaurant != null) {
                                if (restaurantChangeStreamEvent.getOperationType()
                                        == OperationType.DELETE) {
                                    this.restaurantESRepository
                                            .delete(restaurant)
                                            .log()
                                            .doOnError(
                                                    error ->
                                                            log.error(
                                                                    "Error deleting restaurant: {}",
                                                                    error.getMessage()))
                                            .subscribe();
                                } else if (restaurantChangeStreamEvent.getOperationType()
                                                == OperationType.REPLACE
                                        || restaurantChangeStreamEvent.getOperationType()
                                                == OperationType.INSERT) {
                                    this.restaurantESRepository
                                            .save(restaurant)
                                            .log()
                                            .doOnError(
                                                    error ->
                                                            log.error(
                                                                    "Error saving restaurant: {}",
                                                                    error.getMessage()))
                                            .subscribe();
                                }
                            } else {
                                ChangeStreamDocument<Document> eventRaw =
                                        restaurantChangeStreamEvent.getRaw();
                                if (Objects.requireNonNull(eventRaw).getOperationType()
                                        == OperationType.DELETE) {
                                    if (eventRaw.getDocumentKey() != null) {
                                        var objectId =
                                                eventRaw.getDocumentKey()
                                                        .get("_id")
                                                        .asObjectId()
                                                        .getValue()
                                                        .toString();
                                        this.restaurantESRepository
                                                .deleteById(objectId)
                                                .doOnError(
                                                        error ->
                                                                log.error(
                                                                        "Error deleting restaurant by ID: {}",
                                                                        error.getMessage()))
                                                .subscribe();
                                    } else {
                                        log.warn(
                                                "Document key is null for DELETE operation. Cannot delete restaurant from repository.");
                                    }
                                }
                            }

                            this.changeStreamResumeRepository
                                    .update(restaurantChangeStreamEvent.getBsonTimestamp())
                                    .log()
                                    .retryWhen(
                                            Retry.backoff(3, Duration.ofSeconds(1))
                                                    .maxBackoff(Duration.ofSeconds(10)))
                                    .doOnError(
                                            error ->
                                                    log.error(
                                                            "Failed to update resume token: {}",
                                                            error.getMessage()))
                                    .subscribe();
                        });
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
