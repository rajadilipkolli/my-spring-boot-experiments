package com.example.graphql.model.response;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        Long id,
        String title,
        String content,
        boolean published,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        LocalDateTime publishedAt,
        PostDetailsResponse details,
        List<TagResponse> tags) {}
