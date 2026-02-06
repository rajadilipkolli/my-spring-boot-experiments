package com.example.highrps.model.response;

import com.example.highrps.entities.PostRedis;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public static final JsonMapper JSON_MAPPER = new JsonMapper();

    public static PostResponse fromJson(String cached) {
        return JSON_MAPPER.readValue(cached, PostResponse.class);
    }

    public static String toJson(PostResponse resp) {
        return JSON_MAPPER.writeValueAsString(resp);
    }

    public static PostResponse fromRedis(PostRedis postRedis) {
        return new PostResponse(
                postRedis.getTitle(),
                postRedis.getContent(),
                postRedis.getPublished(),
                postRedis.getCreatedAt(),
                postRedis.getModifiedAt(),
                postRedis.getPublishedAt(),
                null,
                new ArrayList<>());
    }
}
