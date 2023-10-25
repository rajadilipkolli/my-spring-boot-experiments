package com.example.graphql.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PostCommentRequest(
        @NotBlank(message = "CommentTitle must not be blank") String title,
        @NotBlank(message = "CommentContent must not be blank") String content,
        @Positive(message = "PostId must be greater than 0") Long postId,
        Boolean published) {}
