package com.example.graphql.repositories;

import com.example.graphql.entities.AuthorEntity;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<AuthorEntity, Long> {

    Optional<AuthorEntity> findByEmailAllIgnoreCase(String email);

    AuthorEntity getReferenceByEmail(String email);

    Window<AuthorEntity> findAllBy(ScrollPosition position, Limit limit, Sort sort);
}
