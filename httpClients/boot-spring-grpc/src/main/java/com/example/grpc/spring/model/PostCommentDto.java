package com.example.grpc.spring.model;

public record PostCommentDto(Long id, Long postId, String review) {}
