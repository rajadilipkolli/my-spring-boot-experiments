package com.example.graphql.repositories;

import com.example.graphql.entities.PostCommentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostCommentEntity, Long> {
    List<PostCommentEntity> findByPostEntity_IdIn(List<Long> ids);
}
