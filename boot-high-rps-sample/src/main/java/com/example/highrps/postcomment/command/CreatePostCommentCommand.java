package com.example.highrps.postcomment.command;

/**
 * Command to create a new post comment.
 */
public record CreatePostCommentCommand(String title, String content, Long postId, Boolean published) {
    public CreatePostCommentCommand {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content must not be blank");
        }
        if (postId == null) {
            throw new IllegalArgumentException("postId must not be null");
        }
    }
}
