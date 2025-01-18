package com.example.mongoes.repository.mongodb;

import com.mongodb.client.result.UpdateResult;
import org.bson.BsonTimestamp;
import reactor.core.publisher.Mono;

public interface CustomChangeStreamResumeRepository {

    Mono<UpdateResult> update(BsonTimestamp resumeToken);
}
