package com.example.highrps.repository;

import com.example.highrps.entities.AuthorEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AuthorRepository extends JpaRepository<AuthorEntity, Long> {

    boolean existsByEmailIgnoreCase(String email);

    @Transactional
    long deleteByEmailInIgnoreCase(List<String> emailIds);
}
