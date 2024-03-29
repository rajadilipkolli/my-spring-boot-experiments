package com.example.graphql.querydsl.repositories;

import com.example.graphql.querydsl.entities.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface TagRepository extends JpaRepository<Tag, Long>, QuerydslPredicateExecutor<Tag> {

    Optional<Tag> findByName(String name);
}
