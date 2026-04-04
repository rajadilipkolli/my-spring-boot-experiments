package com.example.highrps.postcomment.command;

import com.example.highrps.postcomment.domain.vo.PostCommentId;

/**
 * Command to update an existing post comment.
 */
public record UpdatePostCommentCommand(
        PostCommentId commentId, Long postId, String title, String content, Boolean published) {
    public UpdatePostCommentCommand {
        if (commentId == null) {
            throw new IllegalArgumentException("commentId must not be null");
        }
        if (postId == null) {
            throw new IllegalArgumentException("postId must not be null");
        }
    }
}
