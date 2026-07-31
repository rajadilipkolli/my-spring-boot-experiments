package com.example.highrps.post.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.highrps.common.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(
        properties = {"app.kafka.publish-time-out-ms=100", "spring.kafka.producer.properties.max.block.ms=100"})
class PostPublishTimeoutIT extends AbstractIntegrationTest {

    @BeforeEach
    void setup() {
        // Pause Kafka to simulate failure
        kafkaContainer
                .getDockerClient()
                .pauseContainerCmd(kafkaContainer.getContainerId())
                .exec();
    }

    @AfterEach
    void tearDown() {
        // Unpause Kafka to recover the environment for other tests
        kafkaContainer
                .getDockerClient()
                .unpauseContainerCmd(kafkaContainer.getContainerId())
                .exec();
    }

    @Test
    void createPost_whenKafkaIsPaused_shouldReturn503() throws Exception {
        String payload = """
            {
                "title": "Timeout Test",
                "content": "This should timeout because Kafka is paused",
                "email": "test@example.com",
                "details": {
                    "detailsKey": "key",
                    "createdBy": "test-user"
                }
            }
            """;

        var response = mockMvcTester
                .post()
                .uri("/api/posts")
                .contentType("application/json")
                .content(payload)
                .exchange();

        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());

        // Assert ProblemDetail shape
        ProblemDetail body = jsonMapper.readValue(response.getResponse().getContentAsString(), ProblemDetail.class);
        assertThat(body.getTitle()).isEqualTo("Service Unavailable");
        assertThat(body.getDetail()).asString().contains("Failed to publish create post event");
    }
}
