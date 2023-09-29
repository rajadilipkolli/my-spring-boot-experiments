package com.example.jooq.r2dbc.model.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostCommentResponse(UUID id, String content, LocalDateTime createdAt) {}
