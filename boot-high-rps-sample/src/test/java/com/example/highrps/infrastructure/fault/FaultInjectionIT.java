package com.example.highrps.infrastructure.fault;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.highrps.common.AbstractIntegrationTest;
import com.example.highrps.post.command.CreatePostCommand;
import com.example.highrps.post.domain.requests.PostDetailsRequest;
import com.example.highrps.shared.IdGenerator;
import java.util.List;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FaultInjectionIT extends AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(FaultInjectionIT.class);

    @Test
    @DisplayName("Should return HTTP 503 (KafkaPublishException) on command path during broker outage")
    void shouldFailSafelyDuringBrokerOutage() {
        log.info("Pausing Kafka container to simulate broker outage...");
        kafkaContainer
                .getDockerClient()
                .pauseContainerCmd(kafkaContainer.getContainerId())
                .exec();

        try {
            Long postId = IdGenerator.generateLong();
            CreatePostCommand createCmd = new CreatePostCommand(
                    postId,
                    "Fault Injection Post",
                    "Content",
                    "author@test.com",
                    true,
                    new PostDetailsRequest("fault-key", "tester"),
                    List.of());

            // Act & Assert
            CompletionException exception = assertThrows(CompletionException.class, () -> {
                postCommandService.createPost(createCmd).join();
            });

            assertThat(exception.getCause())
                    .isInstanceOf(com.example.highrps.shared.KafkaPublishPendingException.class);
            assertThat(exception.getCause().getMessage()).contains("Publish for create post event is still pending");
        } finally {
            log.info("Unpausing Kafka container...");
            kafkaContainer
                    .getDockerClient()
                    .unpauseContainerCmd(kafkaContainer.getContainerId())
                    .exec();
        }
    }
}
