package com.example.learning.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record PostCommentRequest(
        @NotBlank(message = "Title of post comment is mandatory") @Size(max = 255, message = "Title must not exceed 255 characters") String title,

        @NotBlank(message = "Review of post is mandatory") @Size(max = 10000, message = "Review must not exceed 10000 characters") String review,

        boolean published,
        LocalDateTime publishedAt) {}
