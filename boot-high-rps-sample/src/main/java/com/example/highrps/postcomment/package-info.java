/**
 * PostComment Management Module
 * <p>
 * This module handles all post comment operations following CQRS pattern.
 * <p>
 * Public API: {@link com.example.highrps.postcomment.api}
 * <p>
 * Allowed dependencies: post (for post validation)
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "PostComment Management",
        type = org.springframework.modulith.ApplicationModule.Type.OPEN,
        allowedDependencies = {"post", "author", "repository", "shared", "infrastructure", "entities"})
package com.example.highrps.postcomment;
