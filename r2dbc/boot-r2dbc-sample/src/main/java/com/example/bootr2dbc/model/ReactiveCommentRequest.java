package com.example.bootr2dbc.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ReactiveCommentRequest(
        @NotBlank(message = "Content may not be blank") String content, @Positive Long postId) {}
