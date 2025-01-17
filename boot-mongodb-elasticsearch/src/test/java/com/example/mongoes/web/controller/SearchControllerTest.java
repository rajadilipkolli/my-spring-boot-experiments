package com.example.mongoes.web.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.example.mongoes.web.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(SearchController.class)
class SearchControllerValidationTest {

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private SearchService searchService;

    @Test
    void whenQueryIsBlank_thenBadRequest() {
        webTestClient.get().uri("/search/borough?query=").exchange().expectStatus().isBadRequest();
    }

    @Test
    void whenLimitExceeds100_thenBadRequest() {
        webTestClient
                .get()
                .uri("/search/borough?query=test&limit=101")
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void whenOffsetIsNegative_thenBadRequest() {
        webTestClient
                .get()
                .uri("/search/borough?query=test&offset=-1")
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void whenValidParameters_thenOk() {
        given(searchService.searchMatchBorough(anyString(), anyInt(), anyInt()))
                .willReturn(Mono.just(Flux.empty()));

        webTestClient
                .get()
                .uri("/search/borough?query=test&limit=10&offset=0")
                .exchange()
                .expectStatus()
                .isOk();
    }
}
