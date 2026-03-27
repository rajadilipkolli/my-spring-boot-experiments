package com.example.highrps.postcomment.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.highrps.infrastructure.redis.DeletionMarkerHandler;
import com.example.highrps.post.query.PostQueryService;
import com.example.highrps.postcomment.domain.PostCommentMapper;
import com.example.highrps.postcomment.domain.events.PostCommentCreatedEvent;
import com.example.highrps.postcomment.domain.events.PostCommentDeletedEvent;
import com.example.highrps.postcomment.domain.events.PostCommentUpdatedEvent;
import com.example.highrps.postcomment.domain.vo.PostCommentId;
import com.example.highrps.postcomment.query.GetPostCommentQuery;
import com.example.highrps.postcomment.query.PostCommentQueryService;
import com.example.highrps.repository.redis.PostCommentRedisRepository;
import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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
    private ApplicationEventPublisher eventPublisher;

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

        // Act
        postCommentCommandService.createComment(command);

        // Assert - verify event was published
        verify(eventPublisher).publishEvent(any(PostCommentCreatedEvent.class));
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

        // Act
        postCommentCommandService.updateComment(updateCommand);

        // Assert - verify event was published
        verify(eventPublisher).publishEvent(any(PostCommentUpdatedEvent.class));
    }

    @Test
    @DisplayName("Should publish PostCommentDeletedEvent when deleting a comment")
    void shouldPublishEventWhenDeletingComment() {
        // Arrange
        Long postId = 3L;

        // Act
        postCommentCommandService.deleteComment(new PostCommentId(1002L), postId);

        // Assert - verify event was published
        verify(eventPublisher).publishEvent(any(PostCommentDeletedEvent.class));
        verify(deletionMarkerHandler).markDeleted(any(String.class), any(String.class));
    }
}
