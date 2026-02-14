package com.example.highrps.postcomment.rest;

import jakarta.validation.constraints.NotBlank;

public record UpdatePostCommentRequest(
        @NotBlank(message = "Title must not be blank") String title,
        @NotBlank(message = "Content must not be blank") String content,
        Boolean published) {}
