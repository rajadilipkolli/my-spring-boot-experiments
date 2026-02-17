package com.example.highrps.postcomment.domain;

public record CreatePostCommentCmd(String title, String content, Long postId, Boolean published) {}
