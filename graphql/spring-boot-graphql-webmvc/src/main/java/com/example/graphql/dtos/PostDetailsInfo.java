package com.example.graphql.dtos;

import java.time.LocalDateTime;

/** A Projection for the {@link com.example.graphql.entities.PostDetails} entity */
public interface PostDetailsInfo {
    Long getId();

    LocalDateTime getCreatedAt();

    String getCreatedBy();
}
