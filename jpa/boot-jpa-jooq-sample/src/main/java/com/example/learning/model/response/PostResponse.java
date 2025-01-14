package com.example.learning.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response record representing a blog post with its associated comments and tags.
 *
 * @param title the title of the post
 * @param content the content of the post
 * @param published indicates if the post is published
 * @param publishedAt timestamp when the post was published
 * @param author username of the post author
 * @param createdAt timestamp when the post was created
 * @param comments list of comments on the post
 * @param tags list of tags associated with the post
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PostResponse(
        String title,
        String content,
        Boolean published,
        LocalDateTime publishedAt,
        String author,
        LocalDateTime createdAt,
        List<PostCommentResponse> comments,
        List<TagResponse> tags) {}
