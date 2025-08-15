package com.example.rest.proxy.model.response;

import java.util.List;

public record PostResponse(Long postId, Long userId, String title, String body, List<PostCommentDto> postComments) {}
