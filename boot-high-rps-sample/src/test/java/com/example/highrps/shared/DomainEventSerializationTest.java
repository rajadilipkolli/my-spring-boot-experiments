package com.example.highrps.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.highrps.author.domain.events.AuthorCreatedEvent;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class DomainEventSerializationTest {

    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        jsonMapper = JsonMapper.builder().build();
    }

    @Test
    @DisplayName("Should serialize schemaVersion (Phase 5 complete)")
    void shouldSerializeSchemaVersion() {
        AuthorCreatedEvent event = new AuthorCreatedEvent(
                "test@example.com", "John", "M", "Doe", 1234567890L, LocalDateTime.of(2026, 1, 1, 0, 0));

        String json = jsonMapper.writeValueAsString(event);

        assertThat(json).contains("\"schemaVersion\":\"1.0\"");
        assertThat(json).contains("\"email\":\"test@example.com\"");
    }

    @Test
    @DisplayName("Should ignore unknown properties and tolerate schemaVersion during rollout")
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

        AuthorCreatedEvent event = jsonMapper.readValue(jsonWithUnknown, AuthorCreatedEvent.class);

        assertThat(event.email()).isEqualTo("test@example.com");
        assertThat(event.firstName()).isEqualTo("John");
        assertThat(event.schemaVersion()).isEqualTo("1.0");
    }
}
