package com.example.graphql.model.response;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record PostResponse(
        String title,
        String content,
        boolean published,
        OffsetDateTime createdAt,
        LocalDateTime modifiedAt,
        LocalDateTime publishedAt,
        PostDetailsResponse details) {}
