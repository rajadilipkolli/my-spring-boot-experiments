package com.example.graphql.dtos;

import java.time.LocalDateTime;

/** A Projection for the {@link com.example.graphql.entities.Post} entity */
public interface PostInfo {
    Long getId();

    String getTitle();

    String getContent();

    LocalDateTime getCreatedOn();

    PostDetailsInfo getDetails();

    AuthorInfo getAuthor();
}
