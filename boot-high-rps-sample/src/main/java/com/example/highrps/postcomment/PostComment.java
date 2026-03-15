package com.example.highrps.postcomment;

/**
 * PostComment aggregate root placeholder for external module references.
 */
public record PostComment(Long id, Long postId, String reviewText) {}
