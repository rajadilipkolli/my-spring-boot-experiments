package com.example.learning.repository;

import com.example.learning.entities.Post;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"details", "comments"})
    Optional<Post> findByTitleAndDetails_CreatedBy(String title, String createdBy);

    boolean existsByTitleIgnoreCase(String title);

    @Transactional
    @Modifying
    @Query("delete from Post p where p.title = :title and p.details.createdBy = :createdBy")
    int deleteByTitleAndCreatedBy(@Param("title") String title, @Param("createdBy") String createdBy);

    boolean existsByTitleAndDetails_CreatedBy(String title, String userName);
}
