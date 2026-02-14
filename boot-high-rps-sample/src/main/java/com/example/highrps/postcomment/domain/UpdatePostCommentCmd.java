package com.example.highrps.postcomment.domain;

import com.example.highrps.postcomment.domain.vo.PostCommentId;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;

public record UpdatePostCommentCmd(
        @Valid PostCommentId commentId,
        Long postId,
        String title,
        String content,
        OffsetDateTime createdAt,
        Boolean published) {}
