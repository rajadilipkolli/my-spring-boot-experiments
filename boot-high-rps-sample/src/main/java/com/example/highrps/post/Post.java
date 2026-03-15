package com.example.highrps.post;

/**
 * Post aggregate root placeholder for external module references.
 * In Spring Modulith, this serves as the public-facing type other modules can
 * reference.
 * The actual JPA entity (PostEntity) remains internal to the module.
 */
public record Post(Long postId, String title, String content, String authorEmail, boolean published) {}
