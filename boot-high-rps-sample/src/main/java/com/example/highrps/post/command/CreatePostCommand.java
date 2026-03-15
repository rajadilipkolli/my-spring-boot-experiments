package com.example.highrps.post.command;

/**
 * Command to create a new post.
 */
public record CreatePostCommand(Long postId, String title, String content, String authorEmail, Boolean published) {
    public CreatePostCommand {
        if (postId == null) {
            throw new IllegalArgumentException("postId must not be null");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content must not be blank");
        }
        if (authorEmail == null || authorEmail.isBlank()) {
            throw new IllegalArgumentException("authorEmail must not be blank");
        }
    }
}
