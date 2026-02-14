package com.example.highrps.postcomment.domain;

import com.example.highrps.postcomment.domain.vo.PostCommentId;

public record GetPostCommentQuery(Long postId, PostCommentId commentId) {}
