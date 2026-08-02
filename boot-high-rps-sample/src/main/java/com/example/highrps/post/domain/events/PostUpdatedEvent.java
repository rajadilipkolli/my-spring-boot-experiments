package com.example.highrps.post.domain.events;

import com.example.highrps.post.domain.PostDetailsResponse;
import com.example.highrps.post.domain.TagResponse;
import com.example.highrps.shared.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain event published when a post is updated.
 * This event is externalized to Kafka topic 'posts-aggregates'.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PostUpdatedEvent(
        Long postId,
        String title,
        String content,
        String authorEmail,
        boolean published,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        PostDetailsResponse details,
        List<TagResponse> tags)
        implements DomainEvent {}
