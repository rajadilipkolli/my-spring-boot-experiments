package com.example.jooq.r2dbc;

import com.example.jooq.r2dbc.common.AbstractIntegrationTest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

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
