package com.example.graphql.querydsl.model.request;

import jakarta.validation.constraints.NotEmpty;

public record PostCommentRequest(@NotEmpty(message = "Text cannot be empty") String text) {}
