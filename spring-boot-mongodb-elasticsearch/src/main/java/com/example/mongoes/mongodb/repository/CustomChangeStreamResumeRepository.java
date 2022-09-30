package com.example.mongoes.mongodb.repository;

import com.mongodb.client.result.UpdateResult;
import reactor.core.publisher.Mono;

public interface CustomChangeStreamResumeRepository {

    Mono<UpdateResult> update(String resumeToken);
}
