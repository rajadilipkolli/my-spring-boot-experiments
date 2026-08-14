package com.example.highrps.post.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    boolean existsByPostRefId(Long postRefId);

    @EntityGraph(attributePaths = {"authorEntity"})
    Optional<PostEntity> findByPostRefId(Long postRefId);

    @EntityGraph(attributePaths = {"tags", "details", "authorEntity", "tags.tagEntity"})
    List<PostEntity> findByPostRefIdIn(List<Long> postRefIds);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PostEntity p WHERE p.postRefId IN :postRefIds")
    long deleteByPostRefIdIn(@Param("postRefIds") List<Long> postRefIds);
}
