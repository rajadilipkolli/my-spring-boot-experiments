package com.example.mongoes.repository.mongodb;

import com.example.mongoes.document.ChangeStreamResume;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ChangeStreamResumeRepository
        extends ReactiveMongoRepository<ChangeStreamResume, String>,
                CustomChangeStreamResumeRepository {

    Mono<ChangeStreamResume> findFirstByOrderByResumeTimestampDesc();
}
