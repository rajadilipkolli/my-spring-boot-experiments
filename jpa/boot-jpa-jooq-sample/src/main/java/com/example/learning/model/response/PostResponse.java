package com.example.learning.model.response;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        String title,
        String content,
        Boolean published,
        LocalDateTime publishedAt,
        String author,
        LocalDateTime createdAt,
        List<PostCommentResponse> comments,
        List<TagResponse> tags) {}
