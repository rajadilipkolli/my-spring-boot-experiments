package com.example.grpc.spring.repositories;

import com.example.grpc.spring.entities.PostCommentEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PostCommentRepository extends JpaRepository<PostCommentEntity, Long> {
    List<PostCommentEntity> findByPostId(Long postId);

    Optional<PostCommentEntity> findByPost_IdAndId(Long postId, Long commentId);

    @Transactional
    long deleteByPost_IdAndId(Long postId, Long id);
}
