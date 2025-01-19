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
import java.util.List;
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
import reactor.core.scheduler.Schedulers;

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
                .subscribe();
    }

    public Flux<ChangeStreamEvent<Restaurant>> changeStreamProcessor() {
        return reactiveMongoTemplate
                .changeStream(Restaurant.class)
                .watchCollection(AppConstants.RESTAURANT_COLLECTION)
                .resumeAt(getChangeStreamOption())
                .listen()
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
                                            .subscribe();
                                } else if (restaurantChangeStreamEvent.getOperationType()
                                                == OperationType.REPLACE
                                        || restaurantChangeStreamEvent.getOperationType()
                                                == OperationType.INSERT) {
                                    this.restaurantESRepository.save(restaurant).log().subscribe();
                                }
                            } else {
                                ChangeStreamDocument<Document> eventRaw =
                                        restaurantChangeStreamEvent.getRaw();
                                if (Objects.requireNonNull(eventRaw).getOperationType()
                                        == OperationType.DELETE) {
                                    assert eventRaw.getDocumentKey() != null;
                                    var objectId =
                                            eventRaw.getDocumentKey()
                                                    .get("_id")
                                                    .asObjectId()
                                                    .getValue()
                                                    .toString();
                                    this.restaurantESRepository.deleteById(objectId).subscribe();
                                }
                            }

                            this.changeStreamResumeRepository
                                    .update(restaurantChangeStreamEvent.getBsonTimestamp())
                                    .log()
                                    .subscribe();
                        });
    }

    private ChangeStreamOptions getChangeStreamOption() {
        List<ChangeStreamResume> resumeTokenList = getResumeToken();
        ChangeStreamOptions.ChangeStreamOptionsBuilder changeStreamOptionsBuilder;
        if (resumeTokenList.isEmpty()) {
            // Scenario where MongoDb is started freshly hence resumeToken is empty
            changeStreamOptionsBuilder =
                    ChangeStreamOptions.builder()
                            .resumeAt(new BsonTimestamp(Instant.now().getEpochSecond()));
        } else {
            changeStreamOptionsBuilder =
                    ChangeStreamOptions.builder()
                            .resumeAt(resumeTokenList.getFirst().getResumeTimestamp());
        }
        changeStreamOptionsBuilder.returnFullDocumentOnUpdate();
        return changeStreamOptionsBuilder.build();
    }

    private List<ChangeStreamResume> getResumeToken() {
        return this.changeStreamResumeRepository.findAll().toStream().toList();
    }
}
