package com.example.highrps.repository;

import com.example.highrps.entities.AuthorEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<AuthorEntity, Long> {

    Optional<AuthorEntity> findByEmailAllIgnoreCase(String email);

    AuthorEntity getReferenceByEmail(String email);

    boolean existsByEmail(String email);

    void deleteByEmail(String email);
}
