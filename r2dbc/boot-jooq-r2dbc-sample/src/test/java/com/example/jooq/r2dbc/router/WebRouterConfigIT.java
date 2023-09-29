package com.example.jooq.r2dbc.router;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jooq.r2dbc.common.AbstractIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;

class WebRouterConfigIT extends AbstractIntegrationTest {

    @Test
    void willLoadPosts() {
        this.webTestClient
                .get()
                .uri("/posts")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$[*].title")
                .value((List<String> titles) -> assertThat(titles).containsAnyOf("jooq test"));
    }
}
