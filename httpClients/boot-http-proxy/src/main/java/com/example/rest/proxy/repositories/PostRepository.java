package com.example.rest.proxy.repositories;

import com.example.rest.proxy.entities.Post;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = "postComments")
    Optional<Post> findById(Long id);
}
