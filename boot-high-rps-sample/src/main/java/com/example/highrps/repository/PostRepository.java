package com.example.highrps.repository;

import com.example.highrps.entities.PostEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    @EntityGraph(attributePaths = {"tags", "details", "authorEntity", "tags.tagEntity"})
    @Override
    Optional<PostEntity> findById(Long aLong);

    @EntityGraph(attributePaths = {"tags", "details", "authorEntity", "tags.tagEntity"})
    Optional<PostEntity> findByTitle(String title);

    boolean existsByTitle(String title);

    void deleteByTitle(String title);
}
