package com.example.highrps.post.query;

import com.example.highrps.post.domain.PostDetailsResponse;
import com.example.highrps.post.domain.PostEntity;
import com.example.highrps.post.domain.PostRedis;
import com.example.highrps.post.domain.TagEntity;
import com.example.highrps.post.domain.TagResponse;
import com.example.highrps.post.domain.requests.NewPostRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Projection for post read model.
 * This is the read-side representation optimized for queries.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PostProjection(
        Long postId,
        String title,
        String content,
        String authorEmail,
        boolean published,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        PostDetailsResponse details,
        List<TagResponse> tags) {

    /**
     * Maps a database post to its read projection within the active transaction.
     *
     * @param entity the database post
     * @return the fully populated post projection
     */
    static PostProjection fromEntity(PostEntity entity) {
        return new PostProjection(
                entity.getPostRefId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getAuthorEntity().getEmail(),
                entity.isPublished(),
                entity.getPublishedAt(),
                entity.getCreatedAt(),
                entity.getModifiedAt(),
                entity.getDetails() == null
                        ? null
                        : new PostDetailsResponse(
                                entity.getDetails().getDetailsKey(),
                                entity.getDetails().getCreatedAt(),
                                entity.getDetails().getCreatedBy()),
                entity.getTags().stream()
                        .map(postTag -> {
                            TagEntity tag = postTag.getTagEntity();
                            return new TagResponse(tag.getId(), tag.getTagName(), tag.getTagDescription());
                        })
                        .toList());
    }

    static PostProjection fromNewPostRequest(NewPostRequest request) {
        return new PostProjection(
                request.postId(),
                request.title(),
                request.content(),
                request.email(),
                request.published() != null && request.published(),
                request.publishedAt(),
                request.createdAt(),
                request.modifiedAt(),
                request.details(),
                request.tags());
    }

    /**
     * Maps a Redis post to its read projection.
     *
     * @param postRedis the cached post
     * @return the post projection
     */
    static PostProjection fromRedis(PostRedis postRedis) {
        return new PostProjection(
                postRedis.getId(),
                postRedis.getTitle(),
                postRedis.getContent(),
                postRedis.getAuthorEmail(),
                postRedis.isPublished(),
                postRedis.getPublishedAt(),
                postRedis.getCreatedAt(),
                postRedis.getModifiedAt(),
                postRedis.getDetails(),
                postRedis.getTags());
    }
}
