package com.example.graphql.projections;

import com.example.graphql.entities.PostEntity;
import java.time.LocalDateTime;

/** A Projection for the {@link PostEntity} entity */
public interface PostInfo {
    Long getId();

    String getTitle();

    String getContent();

    LocalDateTime getCreatedAt();

    PostDetailsInfo getDetails();

    AuthorInfo getAuthorEntity();
}
