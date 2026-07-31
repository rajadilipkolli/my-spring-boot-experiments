package com.example.highrps.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.highrps.author.domain.events.AuthorCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DomainEventSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Should serialize domain event with schemaVersion=1.0 by default")
    void shouldSerializeWithSchemaVersion() throws Exception {
        AuthorCreatedEvent event = new AuthorCreatedEvent(
                "test@example.com", "John", "M", "Doe", 1234567890L, LocalDateTime.of(2026, 1, 1, 0, 0));

        String json = objectMapper.writeValueAsString(event);

        assertThat(json).contains("\"schemaVersion\":\"1.0\"");
        assertThat(json).contains("\"email\":\"test@example.com\"");
    }

    @Test
    @DisplayName("Should ignore unknown properties and deserialize event without schemaVersion")
    void shouldIgnoreUnknownProperties() throws Exception {
        String jsonWithUnknown = """
                {
                  "email": "test@example.com",
                  "firstName": "John",
                  "lastName": "Doe",
                  "mobile": 1234567890,
                  "createdAt": [2026, 1, 1, 0, 0],
                  "schemaVersion": "1.0",
                  "unknownProperty": "someValue"
                }
                """;

        AuthorCreatedEvent event = objectMapper.readValue(jsonWithUnknown, AuthorCreatedEvent.class);

        assertThat(event.email()).isEqualTo("test@example.com");
        assertThat(event.firstName()).isEqualTo("John");
    }
}
