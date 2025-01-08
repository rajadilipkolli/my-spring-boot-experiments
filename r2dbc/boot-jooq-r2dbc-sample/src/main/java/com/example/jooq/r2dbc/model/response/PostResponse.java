package com.example.jooq.r2dbc.model.response;

import com.example.jooq.r2dbc.entities.Status;
import java.util.List;
import java.util.UUID;

public record PostResponse(
        UUID id,
        String title,
        String content,
        String createdBy,
        Status status,
        List<PostCommentResponse> comments,
        List<String> tags) {}
