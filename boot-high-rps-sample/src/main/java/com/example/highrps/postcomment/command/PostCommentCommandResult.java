package com.example.highrps.postcomment.command;

import java.time.LocalDateTime;

/**
 * Result returned from post comment command operations.
 */
public record PostCommentCommandResult(
        Long id,
        Long postId,
        String title,
        String content,
        boolean published,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt) {}
