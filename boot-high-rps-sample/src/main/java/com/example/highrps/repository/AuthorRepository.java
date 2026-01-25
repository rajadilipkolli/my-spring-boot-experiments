package com.example.highrps.repository;

import com.example.highrps.entities.AuthorEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AuthorRepository extends JpaRepository<AuthorEntity, Long> {

    Optional<AuthorEntity> findByEmailAllIgnoreCase(String email);

    boolean existsByEmail(String email);

    @Transactional
    void deleteByEmail(String email);
}
