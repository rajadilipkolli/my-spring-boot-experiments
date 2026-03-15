package com.example.highrps.post.command;

import com.example.highrps.post.domain.requests.PostDetailsRequest;
import com.example.highrps.post.domain.requests.TagRequest;
import java.time.LocalDateTime;
import java.util.List;

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
        LocalDateTime modifiedAt,
        PostDetailsRequest details,
        List<TagRequest> tags) {}
