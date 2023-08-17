package com.example.graphql.projections;

import com.example.graphql.entities.PostDetailsEntity;
import java.time.LocalDateTime;

/** A Projection for the {@link PostDetailsEntity} entity */
public interface PostDetailsInfo {
    Long getId();

    String getDetailsKey();

    String getCreatedBy();

    LocalDateTime getCreatedAt();

    LocalDateTime getModifiedAt();

    Long postId();
}
