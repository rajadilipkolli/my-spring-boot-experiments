package com.example.mongoes.mongodb.repository;

import com.example.mongoes.mongodb.domain.Notes;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface NotesRepository extends ReactiveMongoRepository<Notes, String> {
}
