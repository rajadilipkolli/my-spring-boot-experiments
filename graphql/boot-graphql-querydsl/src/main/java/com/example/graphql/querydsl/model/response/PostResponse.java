package com.example.graphql.querydsl.model.response;

import java.time.LocalDateTime;

public record PostResponse(Long id, String title, String content, LocalDateTime createdOn) {}
