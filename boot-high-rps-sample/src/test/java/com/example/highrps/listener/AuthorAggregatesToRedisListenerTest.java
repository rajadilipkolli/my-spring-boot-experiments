package com.example.highrps.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.highrps.author.AuthorRequest;
import com.example.highrps.entities.AuthorRedis;
import com.example.highrps.repository.redis.AuthorRedisRepository;
import java.time.LocalDateTime;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import tools.jackson.databind.json.JsonMapper;

class AuthorAggregatesToRedisListenerTest {

    private AuthorAggregatesToRedisListener listener;
    private RedisTemplate<String, String> redisTemplate;
    private JsonMapper jsonMapper;
    private AuthorRedisRepository authorRedisRepository;
    private ListOperations<String, String> listOperations;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        listOperations = mock(ListOperations.class);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        jsonMapper = JsonMapper.builder().build();
        authorRedisRepository = mock(AuthorRedisRepository.class);
        listener =
                new AuthorAggregatesToRedisListener(redisTemplate, "events:queue", jsonMapper, authorRedisRepository);
    }

    @Test
    void shouldMapCreatedAtAndModifiedAtToAuthorRedis() throws Exception {
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
        assert savedEntity.getEmail().equals(email);
        assert savedEntity.getCreatedAt().equals(request.createdAt());
        assert savedEntity.getModifiedAt().equals(request.modifiedAt());
    }
}
