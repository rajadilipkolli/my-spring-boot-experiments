package com.example.jooq.r2dbc.model.response;

import java.util.List;
import java.util.UUID;

public record PostResponse(
        UUID id,
        String title,
        String content,
        String createdBy,
        List<PostCommentResponse> comments,
        List<String> tags) {}
