package com.example.highrps.infrastructure.fault;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.highrps.common.AbstractIntegrationTest;
import com.example.highrps.post.command.CreatePostCommand;
import com.example.highrps.post.domain.requests.PostDetailsRequest;
import com.example.highrps.shared.IdGenerator;
import com.example.highrps.shared.KafkaPublishPendingException;
import java.util.List;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(
        properties = {
            "app.kafka.publish-time-out-ms=2000",
            "spring.kafka.producer.properties.max.block.ms=2000",
            "spring.kafka.producer.properties.request.timeout.ms=1000",
            "spring.kafka.producer.properties.delivery.timeout.ms=2000"
        })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FaultInjectionIT extends AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(FaultInjectionIT.class);

    @Test
    @DisplayName("Should return HTTP 503 (KafkaPublishException) on command path during broker outage")
    void shouldFailSafelyDuringBrokerOutage() throws java.io.IOException {
        log.info("Cutting connection via Toxiproxy to simulate broker outage...");
        kafkaProxy.disable();

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
            CompletionException exception = assertThrows(
                    CompletionException.class,
                    () -> postCommandService.createPost(createCmd).join());

            assertThat(exception.getCause()).isInstanceOf(KafkaPublishPendingException.class);
            assertThat(exception.getCause().getMessage()).contains("Publish for create post event is still pending");
        } finally {
            log.info("Restoring Toxiproxy connection...");
            kafkaProxy.enable();
        }
    }
}
