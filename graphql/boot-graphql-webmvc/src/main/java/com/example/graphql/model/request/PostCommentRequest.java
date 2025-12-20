package com.example.graphql.model.request;

import jakarta.validation.constraints.NotBlank;

public record PostCommentRequest(
        @NotBlank(message = "CommentTitle must not be blank") String title,

        @NotBlank(message = "CommentContent must not be blank") String content,

        @NotBlank(message = "PostId must must not be blank and greater than 0") String postId,

        Boolean published) {}
