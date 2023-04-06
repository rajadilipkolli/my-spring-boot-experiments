package com.example.graphql.repositories;

import com.example.graphql.entities.PostCommentEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostCommentEntity, Long> {
    List<PostCommentEntity> findByPostEntity_IdIn(List<Long> ids);
}
