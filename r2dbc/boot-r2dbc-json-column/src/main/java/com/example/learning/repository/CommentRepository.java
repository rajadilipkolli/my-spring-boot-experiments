package com.example.learning.repository;

import com.example.learning.entity.Comment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface CommentRepository extends R2dbcRepository<Comment, UUID> {

    Flux<Comment> findByPostId(UUID postId);

    Flux<Comment> findAllByPostIdIn(List<UUID> postIds);
}
