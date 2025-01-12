package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.common.AbstractIntegrationTest;
import com.example.demo.notifier.NotificationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ApplicationIntTest extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
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
                .value(aLong -> assertThat(aLong).isEqualTo(1));
    }
}
