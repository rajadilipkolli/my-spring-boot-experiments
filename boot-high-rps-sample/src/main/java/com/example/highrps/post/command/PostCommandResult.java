package com.example.highrps.post.command;

import java.time.LocalDateTime;

/**
 * Result returned from post command operations.
 */
public record PostCommandResult(
        Long postId,
        String title,
        String content,
        String authorEmail,
        boolean published,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt) {}
