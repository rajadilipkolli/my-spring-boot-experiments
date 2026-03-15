package com.example.highrps.author.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;

import com.example.highrps.author.domain.events.AuthorCreatedEvent;
import com.example.highrps.author.domain.events.AuthorDeletedEvent;
import com.example.highrps.author.domain.events.AuthorUpdatedEvent;
import com.example.highrps.repository.redis.AuthorRedisRepository;
import com.github.benmanes.caffeine.cache.Cache;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.json.JsonMapper;

/**
 * Unit tests for AuthorCommandService focusing on event publishing.
 * Uses pure Mockito for fast, isolated testing.
 */
@ExtendWith(MockitoExtension.class)
class AuthorCommandServiceTest {

    @InjectMocks
    private AuthorCommandService authorCommandService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private Cache<String, String> localCache;

    @Mock
    private RedisTemplate<String, String> redis;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private AuthorRedisRepository authorRedisRepository;

    @Mock
    private JsonMapper jsonMapper;

    @Test
    @DisplayName("Should publish AuthorCreatedEvent when creating an author")
    void shouldPublishEventWhenCreatingAuthor() {
        // Arrange
        CreateAuthorCommand command = new CreateAuthorCommand("john99001@example.com", "John", "Doe", 1234567890L);

        // Act
        AuthorCommandResult result = authorCommandService.createAuthor(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("john99001@example.com");

        // Verify event was published
        verify(eventPublisher).publishEvent(any(AuthorCreatedEvent.class));
    }

    @Test
    @DisplayName("Should publish AuthorUpdatedEvent when updating an author")
    void shouldPublishEventWhenUpdatingAuthor() {
        // Arrange
        String email = "jane99002@example.com";
        UpdateAuthorCommand updateCommand = new UpdateAuthorCommand(email, "Jane", "Smith", 9876543210L);

        // Act
        AuthorCommandResult result = authorCommandService.updateAuthor(updateCommand);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(email);

        // Verify event was published
        verify(eventPublisher).publishEvent(any(AuthorUpdatedEvent.class));
    }

    @Test
    @DisplayName("Should publish AuthorDeletedEvent when deleting an author")
    void shouldPublishEventWhenDeletingAuthor() {
        // Arrange
        String email = "delete99003@example.com";

        given(redis.opsForValue()).willReturn(valueOperations);
        willDoNothing().given(valueOperations).set(any(String.class), any(String.class), any(Duration.class));

        // Act
        authorCommandService.deleteAuthor(email);

        // Verify event was published
        verify(eventPublisher).publishEvent(any(AuthorDeletedEvent.class));
    }
}
