package com.example.highrps.post.command;

/**
 * Command to update an existing post.
 */
public record UpdatePostCommand(Long postId, String title, String content, Boolean published) {
    public UpdatePostCommand {
        if (postId == null) {
            throw new IllegalArgumentException("postId must not be null");
        }
    }
}
