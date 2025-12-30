package com.example.learning.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public record PostRequest(
        @NotBlank(message = "Title of post is mandatory") @Size(max = 255, message = "Title must not exceed 255 characters") String title,

        @NotBlank(message = "Content of post can't be Blank") @Size(max = 10000, message = "Content must not exceed 10000 characters") String content,

        boolean published,
        LocalDateTime publishedAt,
        @Valid List<PostCommentRequest> comments,
        @Valid List<TagRequest> tags) {}
