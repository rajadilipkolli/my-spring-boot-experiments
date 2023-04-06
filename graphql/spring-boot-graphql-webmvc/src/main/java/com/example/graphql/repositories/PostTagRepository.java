package com.example.graphql.repositories;

import com.example.graphql.entities.PostTagEntity;
import com.example.graphql.entities.PostTagEntityId;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTagEntity, PostTagEntityId> {

    @EntityGraph(attributePaths = "tagEntity")
    List<PostTagEntity> findByPostEntity_IdIn(List<Long> ids);
}
