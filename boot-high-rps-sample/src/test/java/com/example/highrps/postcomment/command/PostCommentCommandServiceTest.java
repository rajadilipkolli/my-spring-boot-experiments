package com.example.highrps.postcomment.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.highrps.infrastructure.redis.DeletionMarkerHandler;
import com.example.highrps.post.query.PostQueryService;
import com.example.highrps.postcomment.domain.PostCommentMapper;
import com.example.highrps.postcomment.domain.PostCommentRedisRepository;
import com.example.highrps.postcomment.domain.events.PostCommentCreatedEvent;
import com.example.highrps.postcomment.domain.events.PostCommentDeletedEvent;
import com.example.highrps.postcomment.domain.events.PostCommentUpdatedEvent;
import com.example.highrps.postcomment.domain.vo.PostCommentId;
import com.example.highrps.postcomment.query.GetPostCommentQuery;
import com.example.highrps.postcomment.query.PostCommentQueryService;
import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Unit tests for PostCommentCommandService focusing on event publishing.
 * Uses pure Mockito for fast, isolated testing.
 */
@ExtendWith(MockitoExtension.class)
class PostCommentCommandServiceTest {

    @InjectMocks
    private PostCommentCommandService postCommentCommandService;

    @Mock
    private PostQueryService postQueryService;

    @Mock
    private PostCommentQueryService postCommentQueryService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private Cache<String, String> localCache;

    @Mock
    private DeletionMarkerHandler deletionMarkerHandler;

    @Mock
    private PostCommentRedisRepository postCommentRedisRepository;

    @Mock
    private PostCommentMapper postCommentMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MeterRegistry meterRegistry;

    @Test
    @DisplayName("Should publish PostCommentCreatedEvent when creating a comment")
    void shouldPublishEventWhenCreatingComment() {
        // Arrange
        Long postId = 1L;
        when(postQueryService.exists(postId)).thenReturn(true);
        CreatePostCommentCommand command = new CreatePostCommentCommand("Title", "Content", postId, true);
        given(kafkaTemplate.send(anyString(), anyString(), any())).willReturn(CompletableFuture.completedFuture(null));

        PostCommentCommandResult result =
                postCommentCommandService.createComment(command).join();

        // Assert - verify event was published
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<PostCommentCreatedEvent> eventCaptor = ArgumentCaptor.forClass(PostCommentCreatedEvent.class);
        verify(kafkaTemplate).send(eq("post-comments-aggregates"), keyCaptor.capture(), eventCaptor.capture());
        assertThat(keyCaptor.getValue())
                .isEqualTo(String.valueOf(eventCaptor.getValue().commentId()));
    }

    @Test
    @DisplayName("Should publish PostCommentUpdatedEvent when updating a comment")
    void shouldPublishEventWhenUpdatingComment() {
        // Arrange
        Long postId = 2L;
        UpdatePostCommentCommand updateCommand =
                new UpdatePostCommentCommand(new PostCommentId(1001L), postId, "Title", "Content", true);
        given(postCommentQueryService.getCommentById(
                        new GetPostCommentQuery(updateCommand.postId(), updateCommand.commentId())))
                .willReturn(new PostCommentCommandResult(
                        updateCommand.commentId().id(),
                        postId,
                        "Old Title",
                        "Old Content",
                        true,
                        OffsetDateTime.now(),
                        LocalDateTime.now(),
                        LocalDateTime.now()));
        given(kafkaTemplate.send(anyString(), anyString(), any())).willReturn(CompletableFuture.completedFuture(null));

        // Act
        postCommentCommandService.updateComment(updateCommand).join();

        // Assert - verify event was published
        verify(kafkaTemplate)
                .send(
                        eq("post-comments-aggregates"),
                        eq(String.valueOf(updateCommand.commentId().id())),
                        any(PostCommentUpdatedEvent.class));
    }

    @Test
    @DisplayName("Should publish PostCommentDeletedEvent when deleting a comment")
    void shouldPublishEventWhenDeletingComment() {
        // Arrange
        Long postId = 3L;
        PostCommentId commentId = new PostCommentId(1002L);
        given(kafkaTemplate.send(anyString(), anyString(), any())).willReturn(CompletableFuture.completedFuture(null));

        // Act
        postCommentCommandService.deleteComment(commentId, postId).join();

        // Assert - verify event was published
        verify(kafkaTemplate)
                .send(
                        eq("post-comments-aggregates"),
                        eq(String.valueOf(commentId.id())),
                        any(PostCommentDeletedEvent.class));
        verify(deletionMarkerHandler).markDeleted(any(String.class), any(String.class));
    }
}
