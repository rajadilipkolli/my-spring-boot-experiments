package com.example.highrps.postcomment.domain;

import com.example.highrps.postcomment.command.CreatePostCommentCommand;
import com.example.highrps.postcomment.command.UpdatePostCommentCommand;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Request/Event DTO for PostComment operations.
 * Used as Kafka event payload and for internal service communication.
 */
public record PostCommentRequest(
        Long commentId,
        Long postId,
        String title,
        String content,
        Boolean published,
        OffsetDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt) {

    /**
     * Create a new request with updated timestamps.
     */
    public PostCommentRequest withTimestamps(LocalDateTime modifiedAt, LocalDateTime createdAt) {
        return new PostCommentRequest(
                this.commentId,
                this.postId,
                this.title,
                this.content,
                this.published,
                this.publishedAt,
                createdAt,
                modifiedAt);
    }

    /**
     * Create a new request with published timestamp.
     */
    public PostCommentRequest withPublishedAt(OffsetDateTime publishedAt) {
        return new PostCommentRequest(
                this.commentId,
                this.postId,
                this.title,
                this.content,
                this.published,
                publishedAt,
                this.createdAt,
                this.modifiedAt);
    }

    /**
     * Create from CreatePostCommentCmd.
     */
    public static PostCommentRequest fromCreateCmd(CreatePostCommentCommand cmd, Long commentId) {
        OffsetDateTime publishedAt = Boolean.TRUE.equals(cmd.published()) ? OffsetDateTime.now() : null;
        LocalDateTime now = LocalDateTime.now();
        return new PostCommentRequest(
                commentId, cmd.postId(), cmd.title(), cmd.content(), cmd.published(), publishedAt, now, null);
    }

    /**
     * Create from UpdatePostCommentCmd.
     */
    public static PostCommentRequest fromUpdateCmd(UpdatePostCommentCommand cmd, LocalDateTime createdAt) {
        OffsetDateTime publishedAt = Boolean.TRUE.equals(cmd.published()) ? OffsetDateTime.now() : null;
        return new PostCommentRequest(
                cmd.commentId().id(),
                cmd.postId(),
                cmd.title(),
                cmd.content(),
                cmd.published(),
                publishedAt,
                createdAt,
                LocalDateTime.now());
    }
}
