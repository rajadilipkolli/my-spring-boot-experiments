package com.example.highrps.postcomment.rest;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

public record UpdatePostCommentRequest(
        @NotBlank(message = "Title must not be blank") String title,
        @NotBlank(message = "Content must not be blank") String content,
        OffsetDateTime createdAt,
        Boolean published) {}
