package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.common.AbstractIntegrationTest;
import com.example.demo.listener.NotificationEvent;
import com.example.demo.notifier.NotificationRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ApplicationIntTest extends AbstractIntegrationTest {

    @Test
    void shouldSuccessfullyNotifyOnValidChannel() {
        this.webTestClient
                .post()
                .uri("/api/notify")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new NotificationRequest("junitChannel", "junitMessage"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Long.class)
                .value(aLong -> assertThat(aLong).isOne());
    }

    @Test
    void shouldRejectInvalidChannelName() {
        this.webTestClient
                .post()
                .uri("/api/notify")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new NotificationRequest("invalid@channel", "message"))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(ProblemDetail.class)
                .value(problemDetail -> {
                    assertThat(problemDetail.getStatus()).isEqualTo(400);
                    assertThat(problemDetail.getTitle()).isEqualTo("Bad Request");
                    assertThat(problemDetail.getDetail())
                            .isEqualTo("Channel name must contain only letters, numbers, and underscores");
                });
    }

    @Test
    @Disabled
    void shouldReceiveNotificationOnChannel() {
        // Create notification request
        NotificationRequest notificationRequest = new NotificationRequest("junitChannel", "Hello from test!");

        // Start listening to notifications (SSE stream)
        Flux<NotificationEvent> notificationStream = webTestClient
                .get()
                .uri("/notifications/junitChannel")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(NotificationEvent.class)
                .getResponseBody();

        // Send the notification and verify the response
        Mono<Long> postResult = webTestClient
                .post()
                .uri("/api/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(notificationRequest)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Long.class)
                .getResponseBody()
                .single();

        // Combine the sending and receiving logic
        StepVerifier.create(
                        postResult.thenMany(notificationStream.take(1)) // Ensure we wait for the notification event
                        )
                .assertNext(event -> {
                    assertThat(event.channel()).isEqualTo("junitChannel");
                    assertThat(event.message()).isEqualTo("Hello from test!");
                })
                .verifyComplete();
    }
}
