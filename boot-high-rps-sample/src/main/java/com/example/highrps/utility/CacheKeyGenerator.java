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
        return String.join(":", postId.toString(), commentId.toString());
    }

    /**
     * Generate cache key for a post.
     * Format: "title:email" (email lowercased)
     */
    public static String generatePostKey(String title, String email) {
        return String.join(":", title, email.toLowerCase(Locale.ROOT));
    }
}
