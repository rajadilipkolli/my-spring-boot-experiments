package com.example.graphql.querydsl.model.response;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        Long id,
        String title,
        String content,
        LocalDateTime createdOn,
        List<PostCommentResponse> postCommentResponses) {}
