package com.example.graphql.model.request;

public record AddCommentToPostRequest(
        String title, String content, Long postId, Boolean published) {}
