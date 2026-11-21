package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.common.AbstractIntegrationTest;
import com.example.demo.listener.NotificationEvent;
import com.example.demo.notifier.NotificationRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import reactor.core.publisher.Flux;
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
    void shouldReceiveNotificationOnChannel() throws Exception {
        String channel = "junitStreamChannel";
        // Create notification request
        NotificationRequest notificationRequest = new NotificationRequest(channel, "Hello from test!");

        // Start listening to notifications (SSE stream) in background since exchange() blocks until first event
        CompletableFuture<Flux<NotificationEvent>> notificationStreamFuture =
                CompletableFuture.supplyAsync(() -> webTestClient
                        .get()
                        .uri("/notifications/{channel}", channel)
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .exchange()
                        .expectStatus()
                        .isOk()
                        .returnResult(NotificationEvent.class)
                        .getResponseBody());

        // Wait a bit to ensure GET request has reached server and LISTEN is registered
        Thread.sleep(1000);

        // Send the notification and verify the response
        webTestClient
                .post()
                .uri("/api/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(notificationRequest)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Long.class)
                .value(aLong -> assertThat(aLong).isOne());

        Flux<NotificationEvent> notificationStream = notificationStreamFuture.get(5, TimeUnit.SECONDS);

        // Verify the notification event received on the SSE stream
        StepVerifier.create(notificationStream.take(1))
                .assertNext(event -> {
                    assertThat(event.channel()).isEqualTo(channel);
                    assertThat(event.message()).isEqualTo("Hello from test!");
                })
                .verifyComplete();
    }
}
