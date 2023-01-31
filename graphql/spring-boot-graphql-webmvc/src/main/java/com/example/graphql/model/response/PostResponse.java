package com.example.graphql.model.response;

import java.time.LocalDateTime;

public record PostResponse(
        String title,
        String content,
        boolean published,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        LocalDateTime publishedAt,
        PostDetailsResponse details) {}
