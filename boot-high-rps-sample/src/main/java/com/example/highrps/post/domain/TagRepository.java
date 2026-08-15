package com.example.highrps.post.domain;

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TagRepository extends JpaRepository<@NonNull TagEntity, Long> {

    Optional<TagEntity> findByTagNameIgnoreCase(String tagName);

    @Query("select t from TagEntity t where lower(t.tagName) in :tagNames")
    List<TagEntity> findByTagNameInAllIgnoreCase(@Param("tagNames") List<String> tagNames);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("delete from TagEntity t where lower(t.tagName) = lower(:tagName)")
    void deleteByTagNameIgnoreCase(@Param("tagName") String tagName);
}
