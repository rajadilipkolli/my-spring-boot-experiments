package com.example.highrps.post.query;

import com.example.highrps.post.domain.PostDetailsResponse;
import com.example.highrps.post.domain.TagResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Projection for post read model.
 * This is the read-side representation optimized for queries.
 */
public record PostProjection(
        Long postId,
        String title,
        String content,
        String authorEmail,
        boolean published,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        PostDetailsResponse details,
        List<TagResponse> tags) {}
