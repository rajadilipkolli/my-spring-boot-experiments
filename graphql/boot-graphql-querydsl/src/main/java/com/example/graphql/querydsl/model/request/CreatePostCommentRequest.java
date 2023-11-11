package com.example.graphql.querydsl.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

public record CreatePostCommentRequest(
        @NotEmpty(message = "Review cannot be empty") String review,
        @Positive(message = "PostId should be positive") Long postId) {}
