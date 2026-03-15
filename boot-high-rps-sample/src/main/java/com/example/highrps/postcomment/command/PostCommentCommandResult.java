package com.example.highrps.postcomment.command;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Result returned from post comment command operations.
 */
public record PostCommentCommandResult(
        Long id,
        Long postId,
        String title,
        String content,
        boolean published,
        OffsetDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt) {}
