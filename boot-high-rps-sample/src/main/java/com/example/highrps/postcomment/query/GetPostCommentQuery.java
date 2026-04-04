package com.example.highrps.postcomment.query;

import com.example.highrps.postcomment.domain.vo.PostCommentId;

/**
 * Query to retrieve a post comment by ID.
 */
public record GetPostCommentQuery(Long postId, PostCommentId commentId) {
    public GetPostCommentQuery {
        if (postId == null) {
            throw new IllegalArgumentException("postId must not be null");
        }
        if (commentId == null) {
            throw new IllegalArgumentException("commentId must not be null");
        }
    }
}
