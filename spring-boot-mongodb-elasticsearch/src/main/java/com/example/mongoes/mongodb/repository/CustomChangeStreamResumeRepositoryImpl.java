package com.example.mongoes.mongodb.repository;

import com.example.mongoes.document.ChangeStreamResume;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;

import org.bson.BsonValue;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CustomChangeStreamResumeRepositoryImpl implements CustomChangeStreamResumeRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    private static final String field = "resumeToken";

    @Override
    public Mono<UpdateResult> update(BsonValue resumeToken) {
        Query query = new Query();
        Update update = new Update().set(field, resumeToken);
        return reactiveMongoTemplate.upsert(query, update, ChangeStreamResume.class);
    }
}
