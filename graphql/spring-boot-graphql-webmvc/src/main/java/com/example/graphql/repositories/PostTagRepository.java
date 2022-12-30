package com.example.graphql.repositories;

import com.example.graphql.entities.PostTagEntity;
import com.example.graphql.entities.PostTagEntityId;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTagEntity, PostTagEntityId> {

    @EntityGraph(attributePaths = "tag")
    List<PostTagEntity> findByPost_IdIn(List<Long> ids);
}
