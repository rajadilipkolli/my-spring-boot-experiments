package com.example.mongoes.mongodb.repository;

import com.mongodb.client.result.UpdateResult;
import org.bson.BsonValue;
import reactor.core.publisher.Mono;

public interface CustomChangeStreamResumeRepository {

    Mono<UpdateResult> update(BsonValue resumeToken);
}
