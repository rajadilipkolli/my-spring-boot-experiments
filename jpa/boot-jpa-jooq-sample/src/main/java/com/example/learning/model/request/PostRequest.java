package com.example.learning.model.request;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

public record PostRequest(
        @NotBlank(message = "Title of post is mandatory") String title,
        @NotBlank(message = "Content of post can't be Blank") String content,
        boolean published,
        LocalDateTime publishedAt,
        List<PostCommentRequest> comments,
        List<TagRequest> tags) {}
