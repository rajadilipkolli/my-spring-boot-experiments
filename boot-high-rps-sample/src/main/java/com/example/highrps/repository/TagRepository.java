package com.example.highrps.repository;

import com.example.highrps.entities.TagEntity;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<@NonNull TagEntity, Long> {

    Optional<TagEntity> findByTagNameIgnoreCase(String tagName);

    void deleteByTagNameIgnoreCase(String tagName);
}
