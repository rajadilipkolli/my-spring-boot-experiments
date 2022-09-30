package com.example.mongoes.mongodb.repository;

import org.bson.BsonValue;

import com.mongodb.client.result.UpdateResult;
import reactor.core.publisher.Mono;

public interface CustomChangeStreamResumeRepository {

    Mono<UpdateResult> update(BsonValue resumeToken);
}
