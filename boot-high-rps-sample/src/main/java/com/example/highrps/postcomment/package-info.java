/**
 * PostComment Management Module
 * <p>
 * This module handles all post comment operations following CQRS pattern.
 * <p>
 * Public API: {@link com.example.highrps.postcomment.rest.PostCommentController}
 * <p>
 * Allowed dependencies: post , author, repository, shared, infrastructure, entities
 * <p>
 * This module is designed to be open for extension but closed for modification, allowing for future enhancements without altering existing code. It can depend on other modules such as post, author, repository, shared, infrastructure, and entities, but no other module can depend on it, ensuring a clear separation of concerns and maintaining the integrity of the application architecture.
 * <p>
 * The module is structured to support high read performance and scalability, with a focus on efficient data retrieval and comment management. It includes components for handling comment creation, retrieval, and deletion, as well as mapping between domain models and data transfer objects (DTOs) for seamless integration with the rest of the application.
 * <p>
 */
@ApplicationModule(
        displayName = "PostComment Management",
        type = ApplicationModule.Type.OPEN,
        allowedDependencies = {"post", "author", "repository", "shared", "infrastructure", "entities"})
package com.example.highrps.postcomment;

import org.springframework.modulith.ApplicationModule;
