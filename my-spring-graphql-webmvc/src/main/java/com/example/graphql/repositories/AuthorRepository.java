package com.example.graphql.repositories;

import com.example.graphql.entities.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {}
