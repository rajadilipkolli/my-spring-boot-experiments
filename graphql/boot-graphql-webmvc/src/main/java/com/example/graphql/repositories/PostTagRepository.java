package com.example.graphql.repositories;

import com.example.graphql.entities.PostTagEntity;
import com.example.graphql.entities.PostTagId;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTagEntity, PostTagId> {

    @EntityGraph(attributePaths = "tagEntity")
    List<PostTagEntity> findByPostEntity_IdIn(List<Long> ids);
}
