package com.example.mongoes.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.mongoes.document.Restaurant;
import com.example.mongoes.repository.elasticsearch.RestaurantESRepository;
import com.example.mongoes.repository.mongodb.ChangeStreamResumeRepository;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;
import java.lang.reflect.Method;
import java.util.List;
import org.bson.BsonObjectId;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChangeStreamStartupListenerTest {

    @Mock private ReactiveMongoTemplate reactiveMongoTemplate;
    @Mock private RestaurantESRepository restaurantESRepository;
    @Mock private ChangeStreamResumeRepository changeStreamResumeRepository;

    private ChangeStreamStartupListener listener;

    @BeforeEach
    void setUp() {
        listener =
                new ChangeStreamStartupListener(
                        reactiveMongoTemplate,
                        restaurantESRepository,
                        changeStreamResumeRepository);
    }

    @Test
    void processBatch_WithInsertAndUpdates_ShouldBulkSaveAndEmit() throws Exception {
        Restaurant r1 = new Restaurant();
        r1.setId("id1");
        r1.setName("R1");

        Restaurant r2 = new Restaurant();
        r2.setId("id2");
        r2.setName("R2");

        BsonTimestamp ts1 = new BsonTimestamp(100, 1);
        BsonTimestamp ts2 = new BsonTimestamp(200, 1);

        ChangeStreamEvent<Restaurant> insertEvent =
                createEvent(OperationType.INSERT, r1, ts1, null);
        ChangeStreamEvent<Restaurant> updateEvent =
                createEvent(OperationType.UPDATE, r2, ts2, null);

        given(restaurantESRepository.saveAll(anyList())).willReturn(Flux.just(r1, r2));
        given(changeStreamResumeRepository.update(ts2)).willReturn(Mono.empty());

        Method processBatchMethod =
                ChangeStreamStartupListener.class.getDeclaredMethod("processBatch", List.class);
        processBatchMethod.setAccessible(true);

        @SuppressWarnings("unchecked")
        Flux<ChangeStreamEvent<Restaurant>> resultFlux =
                (Flux<ChangeStreamEvent<Restaurant>>)
                        processBatchMethod.invoke(listener, List.of(insertEvent, updateEvent));

        StepVerifier.create(resultFlux).expectNext(insertEvent, updateEvent).verifyComplete();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Restaurant>> captor = ArgumentCaptor.forClass(List.class);
        verify(restaurantESRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).containsExactly(r1, r2);

        verify(changeStreamResumeRepository).update(ts2);
    }

    @Test
    void processBatch_WithDelete_ShouldBulkDeleteById() throws Exception {
        BsonTimestamp ts = new BsonTimestamp(300, 1);
        ObjectId objectId = new ObjectId();
        org.bson.BsonDocument docKey = new org.bson.BsonDocument("_id", new BsonObjectId(objectId));

        ChangeStreamDocument<Document> rawDoc = mock(ChangeStreamDocument.class);
        given(rawDoc.getDocumentKey()).willReturn(docKey);

        ChangeStreamEvent<Restaurant> deleteEvent =
                createEvent(OperationType.DELETE, null, ts, rawDoc);

        given(restaurantESRepository.deleteAllById(anyList())).willReturn(Mono.empty());
        given(changeStreamResumeRepository.update(ts)).willReturn(Mono.empty());

        Method processBatchMethod =
                ChangeStreamStartupListener.class.getDeclaredMethod("processBatch", List.class);
        processBatchMethod.setAccessible(true);

        @SuppressWarnings("unchecked")
        Flux<ChangeStreamEvent<Restaurant>> resultFlux =
                (Flux<ChangeStreamEvent<Restaurant>>)
                        processBatchMethod.invoke(listener, List.of(deleteEvent));

        StepVerifier.create(resultFlux).expectNext(deleteEvent).verifyComplete();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(restaurantESRepository).deleteAllById(captor.capture());
        assertThat(captor.getValue()).containsExactly(objectId.toHexString());

        verify(changeStreamResumeRepository).update(ts);
    }

    @SuppressWarnings("unchecked")
    private ChangeStreamEvent<Restaurant> createEvent(
            OperationType operationType,
            Restaurant body,
            BsonTimestamp timestamp,
            ChangeStreamDocument<Document> raw) {
        ChangeStreamEvent<Restaurant> event = mock(ChangeStreamEvent.class);
        given(event.getOperationType()).willReturn(operationType);
        given(event.getBody()).willReturn(body);
        given(event.getBsonTimestamp()).willReturn(timestamp);
        if (raw != null) {
            given(event.getRaw()).willReturn(raw);
        }
        return event;
    }
}
