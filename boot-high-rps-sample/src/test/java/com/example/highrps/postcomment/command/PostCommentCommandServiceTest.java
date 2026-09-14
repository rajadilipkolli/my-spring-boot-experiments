package com.example.highrps.postcomment.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.highrps.infrastructure.cache.CacheKeyGenerator;
import com.example.highrps.post.query.PostQueryService;
import com.example.highrps.postcomment.domain.PostCommentMapper;
import com.example.highrps.postcomment.domain.PostCommentRedis;
import com.example.highrps.postcomment.domain.PostCommentRedisRepository;
import com.example.highrps.postcomment.domain.PostCommentRequest;
import com.example.highrps.postcomment.domain.events.PostCommentCreatedEvent;
import com.example.highrps.postcomment.domain.events.PostCommentDeletedEvent;
import com.example.highrps.postcomment.domain.events.PostCommentUpdatedEvent;
import com.example.highrps.postcomment.domain.vo.PostCommentId;
import com.example.highrps.postcomment.query.GetPostCommentQuery;
import com.example.highrps.postcomment.query.PostCommentQueryService;
import com.example.highrps.shared.config.AppProperties;
import com.example.highrps.shared.redis.DeletionMarkerHandler;
import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Unit tests for PostCommentCommandService focusing on event publishing.
 * Uses pure Mockito for fast, isolated testing.
 */
@ExtendWith(MockitoExtension.class)
class PostCommentCommandServiceTest {

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

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MeterRegistry meterRegistry;

    /**
     * Creates the command service under test with mocked collaborators.
     */
    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        appProperties.getKafka().setPublishTimeOutMs(5000L);
        postCommentCommandService = new PostCommentCommandService(
                postQueryService,
                postCommentQueryService,
                kafkaTemplate,
                localCache,
                postCommentMapper,
                meterRegistry,
                deletionMarkerHandler,
                postCommentRedisRepository,
                appProperties,
                redisTemplate);
    }

    /**
     * Verifies comment creation publishes a creation event.
     */
    @Test
    @DisplayName("Should publish PostCommentCreatedEvent when creating a comment")
    void shouldPublishEventWhenCreatingComment() {
        // Arrange
        org.mockito.Mockito.lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        org.mockito.Mockito.lenient()
                .when(valueOperations.setIfAbsent(
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any(java.time.Duration.class)))
                .thenReturn(true);

        Long postId = 1L;
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .willReturn(true);
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

    /**
     * Verifies comment updates publish an update event.
     */
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
        given(postCommentMapper.toResultFromRequest(any(PostCommentRequest.class)))
                .willReturn(new PostCommentCommandResult(
                        updateCommand.commentId().id(),
                        postId,
                        updateCommand.title(),
                        updateCommand.content(),
                        true,
                        OffsetDateTime.now(),
                        LocalDateTime.now(),
                        LocalDateTime.now()));

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

    @Test
    @DisplayName("Should serialize Redis writes with the deletion marker")
    void shouldSerializeRedisWritesWithDeletionMarker() throws InterruptedException {
        Long postId = 4L;
        PostCommentId commentId = new PostCommentId(1003L);
        String cacheKey = CacheKeyGenerator.generatePostCommentKey(postId, commentId.id());
        UpdatePostCommentCommand updateCommand =
                new UpdatePostCommentCommand(commentId, postId, "Title", "Content", true);
        PostCommentCommandResult result = new PostCommentCommandResult(
                commentId.id(),
                postId,
                "Title",
                "Content",
                true,
                OffsetDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now());
        AtomicBoolean deleted = new AtomicBoolean();
        CountDownLatch saveStarted = new CountDownLatch(1);
        CountDownLatch releaseSave = new CountDownLatch(1);
        CountDownLatch deletedWriteChecked = new CountDownLatch(1);

        given(postCommentQueryService.getCommentById(new GetPostCommentQuery(postId, commentId)))
                .willReturn(result);
        given(postCommentMapper.toResultFromRequest(any(PostCommentRequest.class)))
                .willReturn(result);
        given(kafkaTemplate.send(anyString(), anyString(), any())).willReturn(CompletableFuture.completedFuture(null));
        given(deletionMarkerHandler.isDeleted(DeletionMarkerHandler.POST_COMMENT, cacheKey))
                .willAnswer(_ -> {
                    if (deleted.get()) {
                        deletedWriteChecked.countDown();
                    }
                    return deleted.get();
                });
        given(postCommentRedisRepository.save(any(PostCommentRedis.class))).willAnswer(invocation -> {
            saveStarted.countDown();
            assertThat(releaseSave.await(5, TimeUnit.SECONDS)).isTrue();
            return invocation.getArgument(0);
        });
        doAnswer(_ -> {
                    deleted.set(true);
                    return null;
                })
                .when(deletionMarkerHandler)
                .markDeleted(DeletionMarkerHandler.POST_COMMENT, cacheKey);

        postCommentCommandService.updateComment(updateCommand).join();
        assertThat(saveStarted.await(5, TimeUnit.SECONDS)).isTrue();

        CompletableFuture<Void> deletion = postCommentCommandService.deleteComment(commentId, postId);
        assertThat(deletion.isDone()).isFalse();
        releaseSave.countDown();
        deletion.join();

        var ordered = inOrder(postCommentRedisRepository, deletionMarkerHandler);
        ordered.verify(postCommentRedisRepository).save(any(PostCommentRedis.class));
        ordered.verify(deletionMarkerHandler).markDeleted(DeletionMarkerHandler.POST_COMMENT, cacheKey);

        postCommentCommandService.updateComment(updateCommand).join();
        assertThat(deletedWriteChecked.await(5, TimeUnit.SECONDS)).isTrue();
        verify(postCommentRedisRepository, times(1)).save(any(PostCommentRedis.class));
    }
}
