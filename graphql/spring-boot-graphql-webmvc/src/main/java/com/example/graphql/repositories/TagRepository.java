package com.example.graphql.repositories;

import com.example.graphql.entities.TagEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<TagEntity, Long> {

    Optional<TagEntity> findByTagNameIgnoreCase(String tagName);
}
