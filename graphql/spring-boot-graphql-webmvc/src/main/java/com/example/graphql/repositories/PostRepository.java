package com.example.graphql.repositories;

import com.example.graphql.entities.PostEntity;
import com.example.graphql.projections.PostInfo;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    @EntityGraph(attributePaths = {"authorEntity"})
    List<PostInfo> findByAuthorEntity_IdIn(List<Long> authorIdsList);
}
