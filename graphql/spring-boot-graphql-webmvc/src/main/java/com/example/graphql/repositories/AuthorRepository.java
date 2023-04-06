package com.example.graphql.repositories;

import com.example.graphql.entities.AuthorEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorRepository extends JpaRepository<AuthorEntity, Long> {

    Optional<AuthorEntity> findByEmailAllIgnoreCase(String email);

    AuthorEntity getReferenceByEmail(String email);
}
