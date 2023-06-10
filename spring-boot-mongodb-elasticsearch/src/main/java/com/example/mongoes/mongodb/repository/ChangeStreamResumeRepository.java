package com.example.mongoes.mongodb.repository;

import com.example.mongoes.document.ChangeStreamResume;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ChangeStreamResumeRepository
        extends ReactiveMongoRepository<ChangeStreamResume, String>,
                CustomChangeStreamResumeRepository {}
