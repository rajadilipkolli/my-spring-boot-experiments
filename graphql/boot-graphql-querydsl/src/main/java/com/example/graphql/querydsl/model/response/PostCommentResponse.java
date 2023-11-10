package com.example.graphql.querydsl.model.response;

import java.time.LocalDateTime;

public record PostCommentResponse(Long id, String review, LocalDateTime createdOn) {}
