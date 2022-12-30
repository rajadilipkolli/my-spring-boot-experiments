package com.example.graphql.repositories;

import com.example.graphql.entities.PostTag;
import com.example.graphql.entities.PostTagId;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTag, PostTagId> {

    @EntityGraph(attributePaths = "tag")
    List<PostTag> findByPost_IdIn(List<Long> ids);
}
