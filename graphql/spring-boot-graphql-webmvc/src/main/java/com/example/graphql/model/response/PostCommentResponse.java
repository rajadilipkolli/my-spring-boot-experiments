package com.example.graphql.model.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record PostCommentResponse(
        Long postId,
        Long commentId,
        String title,
        String content,
        boolean published,
        LocalDateTime publishedAt,
        LocalDateTime createdAt) {}
