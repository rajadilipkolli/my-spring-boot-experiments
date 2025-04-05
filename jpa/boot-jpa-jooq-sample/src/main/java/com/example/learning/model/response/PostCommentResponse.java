package com.example.learning.model.response;

import java.time.OffsetDateTime;

public record PostCommentResponse(String title, String content, boolean published, OffsetDateTime publishedAt) {}
