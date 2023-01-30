package com.example.jooq.r2dbc;

import com.example.jooq.r2dbc.common.AbstractIntegrationTest;
import java.time.Duration;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClientConfigurer;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @BeforeEach
    void setup() {
        // TestContainer is taking more than 5 sec to start postgres and assign connection, hence
        // incrementing the time to fix the issue
        WebTestClientConfigurer configurer =
                (builder, httpHandlerBuilder, connector) ->
                        builder.responseTimeout(Duration.ofMinutes(1));
        this.webTestClient = webTestClient.mutateWith(configurer);
    }

    @Test
    public void willLoadPosts() {
        this.webTestClient
                .get()
                .uri("/posts")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$[*].title")
                .value(
                        (List<String> titles) ->
                                Assertions.assertThat(titles).containsAnyOf("jooq test"));
    }
}
