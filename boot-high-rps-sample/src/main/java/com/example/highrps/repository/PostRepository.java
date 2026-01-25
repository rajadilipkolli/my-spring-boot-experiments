package com.example.highrps.repository;

import com.example.highrps.entities.PostEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    @EntityGraph(attributePaths = {"tags", "details", "authorEntity", "tags.tagEntity"})
    @Override
    Optional<PostEntity> findById(Long aLong);

    @EntityGraph(attributePaths = {"tags", "details", "authorEntity", "tags.tagEntity"})
    Optional<PostEntity> findByTitle(String title);

    boolean existsByTitle(String title);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PostEntity p where p.title = ?1")
    void deleteByTitle(String title);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PostEntity p WHERE p.title IN :titles")
    int deleteAllByTitleIn(@Param("titles") List<String> titles);
}
