package com.example.highrps.repository.jpa;

import com.example.highrps.entities.AuthorEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AuthorRepository extends JpaRepository<AuthorEntity, Long> {

    boolean existsByEmailIgnoreCase(String email);

    @Transactional
    @Modifying
    @Query("delete from AuthorEntity a where lower(a.email) in :emails")
    int deleteByEmailInAllIgnoreCase(@Param("emails") List<String> emails);
}
