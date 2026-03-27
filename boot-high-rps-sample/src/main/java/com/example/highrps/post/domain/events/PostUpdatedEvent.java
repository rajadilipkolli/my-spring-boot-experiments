package com.example.highrps.post.domain.events;

import com.example.highrps.post.domain.requests.PostDetailsRequest;
import com.example.highrps.post.domain.requests.TagRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.modulith.events.Externalized;

/**
 * Domain event published when a post is updated.
 * This event is externalized to Kafka topic 'posts-aggregates'.
 */
@Externalized("posts-aggregates::#{postId}")
public record PostUpdatedEvent(
        Long postId,
        String title,
        String content,
        String authorEmail,
        boolean published,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        PostDetailsRequest details,
        List<TagRequest> tags) {}
