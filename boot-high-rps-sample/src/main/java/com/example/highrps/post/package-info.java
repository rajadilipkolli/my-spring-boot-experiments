/**
 * Post Management Module
 * <p>
 * This module handles all post-related operations following CQRS pattern.
 * <p>
 * Public API: {@link com.example.highrps.post.api}
 * <p>
 * Allowed dependencies: author (for author validation)
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Post Management",
        type = org.springframework.modulith.ApplicationModule.Type.OPEN,
        allowedDependencies = {"author", "repository", "shared", "infrastructure", "entities"})
package com.example.highrps.post;
