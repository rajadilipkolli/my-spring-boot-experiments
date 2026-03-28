package com.example.highrps.author.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.highrps.author.domain.AuthorRedis;
import com.example.highrps.author.domain.AuthorRedisRepository;
import com.example.highrps.author.dto.AuthorRequest;
import com.example.highrps.infrastructure.redis.DeletionMarkerHandler;
import java.time.LocalDateTime;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.RedisTemplate;
import tools.jackson.databind.json.JsonMapper;

class AuthorAggregatesToRedisListenerTest {

    private AuthorAggregatesToRedisListener listener;
    private RedisTemplate<String, String> redisTemplate;
    private JsonMapper jsonMapper;
    private AuthorRedisRepository authorRedisRepository;
    private DeletionMarkerHandler deletionMarkerHandler;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        var listOperations = mock(org.springframework.data.redis.core.ListOperations.class);
        when(redisTemplate.opsForList()).thenReturn(listOperations);

        jsonMapper = JsonMapper.builder().build();
        authorRedisRepository = mock(AuthorRedisRepository.class);
        deletionMarkerHandler = mock(DeletionMarkerHandler.class);
        listener = new AuthorAggregatesToRedisListener(
                redisTemplate, "events:queue", jsonMapper, authorRedisRepository, deletionMarkerHandler);
    }

    @Test
    void shouldMapCreatedAtAndModifiedAtToAuthorRedis() {
        // Arrange
        String email = "test@example.com";
        LocalDateTime now = LocalDateTime.now();
        AuthorRequest request = new AuthorRequest(
                "First", "Middle", "Last", 1234567890L, email, now, now.minusDays(1), now.minusHours(1));

        byte[] payload = jsonMapper.writeValueAsBytes(request);
        ConsumerRecord<String, byte[]> record = new ConsumerRecord<>("authors-aggregates", 0, 0, email, payload);

        // Act
        listener.handleAggregate(record);

        // Assert
        ArgumentCaptor<AuthorRedis> captor = ArgumentCaptor.forClass(AuthorRedis.class);
        verify(authorRedisRepository).save(captor.capture());

        AuthorRedis savedEntity = captor.getValue();
        assertThat(savedEntity.getEmail()).isEqualTo(email);
        assertThat(savedEntity.getCreatedAt()).isEqualTo(request.createdAt());
        assertThat(savedEntity.getModifiedAt()).isEqualTo(request.modifiedAt());
    }
}
