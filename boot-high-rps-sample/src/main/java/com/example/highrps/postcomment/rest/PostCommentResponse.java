package com.example.highrps.postcomment.rest;

import com.example.highrps.postcomment.domain.PostCommentResult;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record PostCommentResponse(
        Long commentId,
        String title,
        String content,
        boolean published,
        OffsetDateTime publishedAt,
        Long postId,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt) {

    public static PostCommentResponse from(PostCommentResult result) {
        return new PostCommentResponse(
                result.commentId(),
                result.title(),
                result.content(),
                result.published(),
                result.publishedAt(),
                result.postId(),
                result.createdAt(),
                result.modifiedAt());
    }
}
