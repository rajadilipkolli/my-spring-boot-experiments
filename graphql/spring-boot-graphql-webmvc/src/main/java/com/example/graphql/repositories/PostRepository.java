package com.example.graphql.repositories;

import com.example.graphql.dtos.PostInfo;
import com.example.graphql.entities.PostEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    @EntityGraph(attributePaths = {"author"})
    List<PostInfo> findByAuthor_IdIn(List<Long> authorIdsList);
}
