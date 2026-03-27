package com.example.highrps.post.query;

/**
 * Query to retrieve a post by ID.
 */
public record PostQuery(Long postId) {
    public PostQuery {
        if (postId == null || postId <= 0) {
            throw new IllegalArgumentException("postId must be a positive number");
        }
    }
}
