package com.example.mongoes.web.controller;

import com.example.mongoes.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class SearchControllerIntTest extends AbstractIntegrationTest {

    @Test
    void searchWildCardBorough() {

        webTestClient
                .get()
                .uri("/search/wildcard?query=manhattan&limit=5&offset=0")
                .exchange()
                .expectStatus()
                .isOk();
    }
}
