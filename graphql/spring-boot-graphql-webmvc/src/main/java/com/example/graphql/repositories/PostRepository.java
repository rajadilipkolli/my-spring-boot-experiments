package com.example.graphql.repositories;

import com.example.graphql.dtos.PostInfo;
import com.example.graphql.entities.Post;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = {"author"})
    List<PostInfo> findByAuthor_IdIn(List<Long> authorIdsList);
}
