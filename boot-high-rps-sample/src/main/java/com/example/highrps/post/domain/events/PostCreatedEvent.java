package com.example.highrps.post.domain.events;

import java.time.LocalDateTime;
import org.springframework.modulith.events.Externalized;

/**
 * Domain event published when a new post is created.
 * This event is externalized to Kafka topic 'posts-aggregates' for downstream
 * consumers.
 */
@Externalized("posts-aggregates::#{postId}")
public record PostCreatedEvent(
        Long postId,
        String title,
        String content,
        String authorEmail,
        boolean published,
        LocalDateTime publishedAt,
        LocalDateTime createdAt) {}
