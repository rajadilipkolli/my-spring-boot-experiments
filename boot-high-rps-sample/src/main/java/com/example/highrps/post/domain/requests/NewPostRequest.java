package com.example.highrps.post.domain.requests;

import com.example.highrps.post.domain.PostDetailsResponse;
import com.example.highrps.post.domain.PostRedis;
import com.example.highrps.post.domain.TagResponse;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NewPostRequest(
        Long postId,
        @NotBlank(message = "PostTitle must not be blank") String title,
        @NotBlank(message = "PostContent must not be blank") String content,

        @NotBlank(message = "Email must not be blank") @Email(message = "Provide valid email") @JsonAlias("authorEmail")
        String email,

        Boolean published,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        @Valid @NotNull PostDetailsResponse details,
        List<@Valid TagResponse> tags)
        implements Serializable {

    /**
     * Maps a post request to a Redis entry, treating a null published flag as false.
     *
     * @param newPostRequest the post data
     * @param postId the identifier to use for the Redis entry
     * @return the populated Redis post
     */
    public static PostRedis toRedis(NewPostRequest newPostRequest, Long postId) {
        return new PostRedis()
                .setId(postId)
                .setTitle(newPostRequest.title())
                .setContent(newPostRequest.content())
                .setPublished(newPostRequest.published() != null && newPostRequest.published())
                .setPublishedAt(newPostRequest.publishedAt())
                .setAuthorEmail(newPostRequest.email())
                .setCreatedAt(newPostRequest.createdAt())
                .setModifiedAt(newPostRequest.modifiedAt())
                .setDetails(newPostRequest.details())
                .setTags(newPostRequest.tags());
    }
}
