package com.example.learning.repository;

import com.example.learning.entities.Post;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByTitleIgnoreCase(String title);
}
