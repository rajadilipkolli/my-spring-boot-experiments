package com.example.highrps.post.query;

/**
 * Query to retrieve a post by ID.
 */
public record PostQuery(Long postId) {
    public PostQuery {
        if (postId == null) {
            throw new IllegalArgumentException("postId must not be null");
        }
    }
}
