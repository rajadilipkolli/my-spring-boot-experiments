package com.example.graphql.model.request;

public record PostCommentRequest(String title, String content, Long postId, Boolean published) {}
