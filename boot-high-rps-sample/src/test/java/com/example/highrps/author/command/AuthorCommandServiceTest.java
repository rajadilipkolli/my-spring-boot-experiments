package com.example.highrps.author.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.highrps.author.domain.events.AuthorCreatedEvent;
import com.example.highrps.author.domain.events.AuthorDeletedEvent;
import com.example.highrps.author.domain.events.AuthorUpdatedEvent;
import com.example.highrps.author.query.AuthorProjection;
import com.example.highrps.author.query.AuthorQuery;
import com.example.highrps.author.query.AuthorQueryService;
import com.example.highrps.infrastructure.redis.DeletionMarkerHandler;
import com.example.highrps.repository.redis.AuthorRedisRepository;
import com.github.benmanes.caffeine.cache.Cache;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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
    private DeletionMarkerHandler deletionMarkerHandler;

    @Mock
    private AuthorRedisRepository authorRedisRepository;

    @Mock
    private JsonMapper jsonMapper;

    @Mock
    private AuthorQueryService authorQueryService;

    @Test
    @DisplayName("Should publish AuthorCreatedEvent when creating an author")
    void shouldPublishEventWhenCreatingAuthor() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        CreateAuthorCommand command = new CreateAuthorCommand(
                "john99001@example.com", "John", null, "Doe", 1234567890L, now, now);

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
        UpdateAuthorCommand updateCommand =
                new UpdateAuthorCommand(email, "Jane", null, "Smith", 9876543210L, LocalDateTime.now());

        AuthorProjection authorProjection = new AuthorProjection(
                email, "Jane", null, "Smith", 9876543210L, LocalDateTime.now(), LocalDateTime.now(), null);
        given(authorQueryService.getAuthor(new AuthorQuery(email))).willReturn(authorProjection);
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

        // Act
        authorCommandService.deleteAuthor(email);

        // Verify event was published
        verify(eventPublisher).publishEvent(any(AuthorDeletedEvent.class));
        verify(deletionMarkerHandler).markDeleted("author", email);
    }
}