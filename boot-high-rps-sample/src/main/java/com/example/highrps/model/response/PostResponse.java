package com.example.highrps.model.response;

import java.time.LocalDateTime;
import java.util.List;
import tools.jackson.databind.json.JsonMapper;

public record PostResponse(
        String title,
        String content,
        boolean published,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        LocalDateTime publishedAt,
        PostDetailsResponse details,
        List<TagResponse> tags) {

    public PostResponse() {
        this(null, null, false, null, null, null, null, null);
    }

    public static PostResponse fromJson(String cached) {
        return new JsonMapper().readValue(cached, PostResponse.class);
    }

    public static String toJson(PostResponse resp) {
        return new JsonMapper().writeValueAsString(resp);
    }
}
