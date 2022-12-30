package com.example.graphql.repositories;

import com.example.graphql.entities.AuthorEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<AuthorEntity, Long> {

    Optional<AuthorEntity> findByEmailAllIgnoreCase(String email);
}
