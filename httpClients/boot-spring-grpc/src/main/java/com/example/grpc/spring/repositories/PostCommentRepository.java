package com.example.grpc.spring.repositories;

import com.example.grpc.spring.entities.PostCommentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostCommentEntity, Long> {
    List<PostCommentEntity> findByPostId(Long postId);
}
