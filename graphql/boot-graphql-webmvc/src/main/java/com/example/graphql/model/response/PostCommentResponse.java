package com.example.graphql.model.response;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.Builder;

@Builder
public record PostCommentResponse(
        Long postId,
        Long commentId,
        String title,
        String content,
        boolean published,
        OffsetDateTime publishedAt,
        LocalDateTime createdAt) {}
