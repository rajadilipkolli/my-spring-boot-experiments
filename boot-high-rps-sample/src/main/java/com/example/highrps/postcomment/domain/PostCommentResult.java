package com.example.highrps.postcomment.domain;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record PostCommentResult(
        Long commentId,
        String title,
        String content,
        boolean published,
        OffsetDateTime publishedAt,
        Long postId,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt) {}
