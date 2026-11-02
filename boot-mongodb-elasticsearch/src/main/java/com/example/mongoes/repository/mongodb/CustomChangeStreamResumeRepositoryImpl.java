package com.example.mongoes.repository.mongodb;

import com.example.mongoes.document.ChangeStreamResume;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonTimestamp;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Mono;

public class CustomChangeStreamResumeRepositoryImpl implements CustomChangeStreamResumeRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    private static final String FIELD_NAME = "resumeTimestamp";

    public CustomChangeStreamResumeRepositoryImpl(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Override
    public Mono<UpdateResult> update(BsonTimestamp resumeTimestamp) {
        Query query = new Query();
        Update update = new Update().set(FIELD_NAME, resumeTimestamp);
        return reactiveMongoTemplate.upsert(query, update, ChangeStreamResume.class);
    }
}
