package com.example.mongoes.mongodb.repository;

import com.example.mongoes.mongodb.domain.Notes;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotesRepository extends MongoRepository<Notes, String> {}
