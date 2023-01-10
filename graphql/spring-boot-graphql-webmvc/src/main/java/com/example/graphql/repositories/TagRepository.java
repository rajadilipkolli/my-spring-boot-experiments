package com.example.graphql.repositories;

import com.example.graphql.entities.TagEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<TagEntity, Long> {

    Optional<TagEntity> findByTagName(String tagName);
}
