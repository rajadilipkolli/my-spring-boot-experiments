package com.example.highrps.utility;

import java.util.Locale;

/**
 * Utility class for generating consistent cache keys across services.
 */
public final class CacheKeyGenerator {

    private CacheKeyGenerator() {
        // Utility class
    }

    /**
     * Generate cache key for a post comment.
     * Format: "postId:commentId"
     */
    public static String generatePostCommentKey(Long postId, Long commentId) {
        return String.join(":", String.valueOf(postId), String.valueOf(commentId));
    }

    /**
     * Generate cache key for a post.
     * Format: "postId:email" (email lowercased)
     */
    public static String generatePostKey(Long postId, String email) {
        return String.join(":", String.valueOf(postId), email.toLowerCase(Locale.ROOT));
    }
}
