package com.example.highrps.postcomment.rest;

import com.example.highrps.postcomment.command.PostCommentCommandResult;
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

    public static PostCommentResponse from(PostCommentCommandResult result) {
        return new PostCommentResponse(
                result.id(),
                result.title(),
                result.content(),
                result.published(),
                null, // publishedAt - not in PostCommentCommandResult
                result.postId(),
                result.createdAt(),
                result.modifiedAt());
    }
}
