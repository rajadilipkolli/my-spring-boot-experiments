package com.example.highrps.post.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.highrps.post.domain.PostDetailsResponse;
import com.example.highrps.post.domain.PostRedis;
import com.example.highrps.post.domain.PostRedisRepository;
import com.example.highrps.post.domain.TagResponse;
import com.example.highrps.post.domain.requests.NewPostRequest;
import com.example.highrps.shared.ResourceNotFoundException;
import com.example.highrps.shared.redis.DeletionMarkerHandler;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class PostQueryServiceTest {

    private Cache<String, String> localCache;

    @Mock
    private PostRedisRepository postRedisRepository;

    @Mock
    private PostReadService postReadService;

    @Mock
    private StreamsBuilderFactoryBean kafkaStreamsFactory;

    @Mock
    private DeletionMarkerHandler deletionMarkerHandler;

    @Mock
    private KafkaStreams kafkaStreams;

    @Mock
    private ReadOnlyKeyValueStore<String, NewPostRequest> keyValueStore;

    private JsonMapper jsonMapper;
    private PostQueryService postQueryService;

    /** Initializes an empty local cache and the query service with mocked dependencies. */
    @BeforeEach
    void setUp() {
        localCache = Caffeine.newBuilder().build();
        jsonMapper = JsonMapper.builder().build();
        postQueryService = new PostQueryService(
                localCache,
                postRedisRepository,
                postReadService,
                kafkaStreamsFactory,
                jsonMapper,
                deletionMarkerHandler);
    }

    /** Verifies that a local cache hit avoids Redis, Kafka Streams, and the database read service. */
    @Test
    @DisplayName("Should return from local cache without hitting PostReadService, Redis, or Streams")
    void shouldReturnFromLocalCacheWithoutHittingPostReadService() {
        Long postId = 101L;
        String expectedJson = "{\"postId\":101,\"title\":\"Cached Title\"}";
        localCache.put(String.valueOf(postId), expectedJson);

        when(deletionMarkerHandler.isDeleted(DeletionMarkerHandler.POST, String.valueOf(postId)))
                .thenReturn(false);

        String result = postQueryService.getPost(new PostQuery(postId));

        assertThat(result).isEqualTo(expectedJson);
        verifyNoInteractions(postReadService);
        verifyNoInteractions(postRedisRepository);
        verifyNoInteractions(kafkaStreamsFactory);
    }

    /** Verifies that a Redis hit warms the local cache without querying the database. */
    @Test
    @DisplayName("Should return from Redis without hitting PostReadService and warm local cache")
    void shouldReturnFromRedisWithoutHittingPostReadService() {
        Long postId = 102L;
        PostRedis postRedis = new PostRedis()
                .setId(postId)
                .setTitle("Redis Title")
                .setContent("Redis Content")
                .setAuthorEmail("author@test.com")
                .setPublished(true)
                .setPublishedAt(LocalDateTime.now())
                .setCreatedAt(LocalDateTime.now())
                .setDetails(new PostDetailsResponse("key", LocalDateTime.now(), "creator"))
                .setTags(List.of(new TagResponse(1L, "tag1", "desc1")));

        when(deletionMarkerHandler.isDeleted(DeletionMarkerHandler.POST, String.valueOf(postId)))
                .thenReturn(false);
        when(postRedisRepository.findById(postId)).thenReturn(Optional.of(postRedis));

        String result = postQueryService.getPost(new PostQuery(postId));

        assertThat(result).contains("Redis Title");
        assertThat(localCache.getIfPresent(String.valueOf(postId))).isNotNull();
        verifyNoInteractions(postReadService);
    }

    /** Verifies that a stream hit populates Redis and the local cache without querying the database. */
    @Test
    @DisplayName("Should return from Kafka Streams without hitting PostReadService and warm Redis and local cache")
    void shouldReturnFromKafkaStreamsWithoutHittingPostReadService() {
        Long postId = 103L;
        NewPostRequest request = new NewPostRequest(
                postId,
                "Streams Title",
                "Streams Content",
                "streams@test.com",
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                new PostDetailsResponse("key", LocalDateTime.now(), "creator"),
                List.of());

        when(deletionMarkerHandler.isDeleted(DeletionMarkerHandler.POST, String.valueOf(postId)))
                .thenReturn(false);
        when(postRedisRepository.findById(postId)).thenReturn(Optional.empty());
        when(kafkaStreamsFactory.getKafkaStreams()).thenReturn(kafkaStreams);
        when(kafkaStreams.state()).thenReturn(KafkaStreams.State.RUNNING);
        when(kafkaStreams.store(any(StoreQueryParameters.class))).thenReturn(keyValueStore);
        when(keyValueStore.get(String.valueOf(postId))).thenReturn(request);

        String result = postQueryService.getPost(new PostQuery(postId));

        assertThat(result).contains("Streams Title");
        assertThat(localCache.getIfPresent(String.valueOf(postId))).isNotNull();
        verify(postRedisRepository).save(any(PostRedis.class));
        verifyNoInteractions(postReadService);
    }

    /** Verifies that cache misses fall back to the database read service and warm the local cache. */
    @Test
    @DisplayName("Should fall back to PostReadService when caches miss and warm local cache")
    void shouldFallbackToPostReadServiceWhenCachesMiss() {
        Long postId = 104L;
        PostProjection projection = new PostProjection(
                postId,
                "DB Title",
                "DB Content",
                "db@test.com",
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                new PostDetailsResponse("key", LocalDateTime.now(), "creator"),
                List.of(new TagResponse(2L, "tag2", "desc2")));

        when(deletionMarkerHandler.isDeleted(DeletionMarkerHandler.POST, String.valueOf(postId)))
                .thenReturn(false);
        when(postRedisRepository.findById(postId)).thenReturn(Optional.empty());
        when(kafkaStreamsFactory.getKafkaStreams()).thenReturn(null);
        when(postReadService.findByPostRefId(postId)).thenReturn(Optional.of(projection));

        String result = postQueryService.getPost(new PostQuery(postId));

        assertThat(result).contains("DB Title");
        assertThat(localCache.getIfPresent(String.valueOf(postId))).isNotNull();
        verify(postReadService).findByPostRefId(postId);
    }

    /** Verifies that an absent database projection produces a resource-not-found exception. */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when PostReadService returns empty")
    void shouldThrowNotFoundWhenPostReadServiceReturnsEmpty() {
        Long postId = 105L;

        when(deletionMarkerHandler.isDeleted(DeletionMarkerHandler.POST, String.valueOf(postId)))
                .thenReturn(false);
        when(postRedisRepository.findById(postId)).thenReturn(Optional.empty());
        when(kafkaStreamsFactory.getKafkaStreams()).thenReturn(null);
        when(postReadService.findByPostRefId(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postQueryService.getPost(new PostQuery(postId)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Post not found for id: " + postId);

        verify(postReadService).findByPostRefId(postId);
    }
}
