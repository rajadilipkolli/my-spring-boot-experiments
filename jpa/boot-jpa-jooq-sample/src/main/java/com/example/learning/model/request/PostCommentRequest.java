package com.example.learning.model.request;

import java.time.LocalDateTime;

public record PostCommentRequest(String title, String review, boolean published, LocalDateTime publishedAt) {}
