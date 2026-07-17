package com.example.highrps.post.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.highrps.infrastructure.redis.DeletionMarkerHandler;
import com.example.highrps.post.domain.PostRedisRepository;
import com.example.highrps.post.domain.events.PostCreatedEvent;
import com.example.highrps.post.domain.events.PostDeletedEvent;
import com.example.highrps.post.domain.events.PostUpdatedEvent;
import com.example.highrps.post.domain.requests.PostDetailsRequest;
import com.example.highrps.post.domain.requests.TagRequest;
import com.example.highrps.post.query.PostQueryService;
import com.github.benmanes.caffeine.cache.Cache;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Unit tests for PostCommandService focusing on event publishing.
 * Uses pure Mockito for fast, isolated testing.
 */
@ExtendWith(MockitoExtension.class)
class PostCommandServiceTest {

    @InjectMocks
    private PostCommandService postCommandService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private Cache<String, String> localCache;

    @Mock
    private DeletionMarkerHandler deletionMarkerHandler;

    @Mock
    private PostRedisRepository postRedisRepository;

    @Mock
    private JsonMapper jsonMapper;

    @Mock
    private PostQueryService postQueryService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Captor
    private ArgumentCaptor<PostCreatedEvent> eventCaptor;

    @Test
    @DisplayName("Should publish PostCreatedEvent when creating a post")
    void shouldPublishEventWhenCreatingPost() {
        // Arrange
        CreatePostCommand command = new CreatePostCommand(
                99001L,
                "Test Title",
                "Test Content",
                "author@example.com",
                true,
                new PostDetailsRequest("key1", "user1"),
                List.of(new TagRequest("t1", "d1")));

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .willReturn(true);
        given(kafkaTemplate.send(anyString(), anyString(), any())).willReturn(CompletableFuture.completedFuture(null));

        // Act
        PostCommandResult result = postCommandService.createPost(command).join();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.postId()).isEqualTo(99001L);
        assertThat(result.title()).isEqualTo("Test Title");

        // Verify event was published
        verify(kafkaTemplate).send(eq("posts-aggregates"), eq("99001"), eventCaptor.capture());
        PostCreatedEvent event = eventCaptor.getValue();
        assertThat(event.postId()).isEqualTo(99001L);
        assertThat(event.title()).isEqualTo("Test Title");
    }

    @Test
    @DisplayName("Should publish PostUpdatedEvent when updating a post")
    void shouldPublishEventWhenUpdatingPost() {
        // Arrange
        UpdatePostCommand updateCommand = new UpdatePostCommand(
                99002L,
                "Updated Title",
                "Updated Content",
                true,
                new PostDetailsRequest("key2", "user2"),
                List.of(new TagRequest("t2", "d2")));

        given(kafkaTemplate.send(anyString(), anyString(), any())).willReturn(CompletableFuture.completedFuture(null));

        // Act
        PostCommandResult result = postCommandService.updatePost(updateCommand).join();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.postId()).isEqualTo(99002L);

        // Verify event was published
        verify(kafkaTemplate).send(eq("posts-aggregates"), eq("99002"), any(PostUpdatedEvent.class));
    }

    @Test
    @DisplayName("Should publish PostDeletedEvent when deleting a post")
    void shouldPublishEventWhenDeletingPost() {
        // Arrange
        Long postId = 99003L;

        given(kafkaTemplate.send(anyString(), anyString(), any())).willReturn(CompletableFuture.completedFuture(null));

        // Act
        postCommandService.deletePost(postId).join();

        // Assert - verify event was published
        verify(kafkaTemplate).send(eq("posts-aggregates"), eq("99003"), any(PostDeletedEvent.class));
        verify(deletionMarkerHandler).markDeleted("post", String.valueOf(postId));
    }
}
