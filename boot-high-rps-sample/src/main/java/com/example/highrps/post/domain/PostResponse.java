package com.example.highrps.post.domain;

import com.example.highrps.post.PostRedis;
import java.time.LocalDateTime;
import java.util.List;
import tools.jackson.databind.json.JsonMapper;

public record PostResponse(
        Long postId,
        String title,
        String content,
        boolean published,
        String authorEmail,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        LocalDateTime publishedAt,
        PostDetailsResponse details,
        List<TagResponse> tags) {

    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    public static PostResponse fromJson(String cached) {
        return JSON_MAPPER.readValue(cached, PostResponse.class);
    }

    public static String toJson(PostResponse resp) {
        return JSON_MAPPER.writeValueAsString(resp);
    }

    public static PostResponse fromRedis(PostRedis postRedis) {
        PostDetailsResponse detailsResponse = null;
        if (postRedis.getDetails() != null) {
            detailsResponse = new PostDetailsResponse(
                    postRedis.getDetails().detailsKey(),
                    postRedis.getCreatedAt(), // Using createdAt as heuristic
                    postRedis.getDetails().createdBy());
        }

        List<TagResponse> tagResponses = List.of();
        if (postRedis.getTags() != null) {
            tagResponses = postRedis.getTags().stream()
                    .map(t -> new TagResponse(null, t.tagName(), t.tagDescription()))
                    .toList();
        }

        return new PostResponse(
                postRedis.getId(),
                postRedis.getTitle(),
                postRedis.getContent(),
                postRedis.isPublished(),
                postRedis.getAuthorEmail(),
                postRedis.getCreatedAt(),
                postRedis.getModifiedAt(),
                postRedis.getPublishedAt(),
                detailsResponse,
                tagResponses);
    }
}
