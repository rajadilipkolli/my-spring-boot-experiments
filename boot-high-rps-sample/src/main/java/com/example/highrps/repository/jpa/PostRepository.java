package com.example.highrps.repository.jpa;

import com.example.highrps.entities.PostEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    boolean existsByPostRefId(Long postRefId);

    Optional<PostEntity> findByPostRefId(Long postRefId);

    @EntityGraph(attributePaths = {"tags", "details", "authorEntity", "tags.tagEntity"})
    List<PostEntity> findByPostRefIdIn(List<Long> postRefIds);

    @Transactional
    @Modifying
    long deleteByPostRefIdIn(List<Long> postRefIds);
}
