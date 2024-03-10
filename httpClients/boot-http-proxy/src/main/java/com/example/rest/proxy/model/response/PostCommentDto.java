package com.example.rest.proxy.model.response;

public record PostCommentDto(Long postId, Long id, String name, String email, String body) {}
