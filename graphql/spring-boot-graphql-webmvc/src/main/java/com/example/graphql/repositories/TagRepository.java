package com.example.graphql.repositories;

import com.example.graphql.entities.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<TagEntity, Long> {}
