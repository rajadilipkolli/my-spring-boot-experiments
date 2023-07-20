package com.example.graphql.projections;

import com.example.graphql.entities.PostEntity;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/** A Projection for the {@link PostEntity} entity */
public interface PostInfo {
    Long getId();

    String getTitle();

    String getContent();

    boolean isPublished();

    OffsetDateTime getCreatedAt();

    LocalDateTime getModifiedAt();

    PostDetailsInfo getDetails();

    AuthorInfo getAuthorEntity();
}
