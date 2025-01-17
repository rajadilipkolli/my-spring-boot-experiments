package com.example.mongoes.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.example.mongoes.response.AggregationSearchResponse;
import com.example.mongoes.web.service.SearchService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

    @Nested
    class BoroughSearchValidation {

        @ParameterizedTest
        @ValueSource(ints = {-100, -1})
        void whenOffsetIsNegative_thenBadRequest(int offset) {
            webTestClient
                    .get()
                    .uri("/search/borough?query=test&offset=" + offset)
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenQueryIsBlank_thenBadRequest() {
            webTestClient
                    .get()
                    .uri("/search/borough?query=")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
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

    @Nested
    class SearchBoolMustValidation {
        @Test
        void whenValidParameters_thenReturns200() {
            given(
                            searchService.queryBoolWithMust(
                                    anyString(), anyString(), anyString(), anyInt(), anyInt()))
                    .willReturn(Mono.just(Flux.empty()));

            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/must/bool")
                                            .queryParam("borough", "Manhattan")
                                            .queryParam("cuisine", "Italian")
                                            .queryParam("name", "Restaurant")
                                            .queryParam("limit", 10)
                                            .queryParam("offset", 0)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isOk();
        }

        @Test
        void whenBoroughIsBlank_thenReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/must/bool")
                                            .queryParam("borough", "")
                                            .queryParam("cuisine", "Italian")
                                            .queryParam("name", "Restaurant")
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenCuisineIsBlank_thenReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/must/bool")
                                            .queryParam("borough", "Manhattan")
                                            .queryParam("cuisine", "")
                                            .queryParam("name", "Restaurant")
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenNameIsBlank_thenReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/must/bool")
                                            .queryParam("borough", "Manhattan")
                                            .queryParam("cuisine", "Italian")
                                            .queryParam("name", "")
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenLimitExceeds100_thenReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/must/bool")
                                            .queryParam("borough", "Manhattan")
                                            .queryParam("cuisine", "Italian")
                                            .queryParam("name", "Restaurant")
                                            .queryParam("limit", 101)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenLimitIsZero_thenSearchMustBoolReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/must/bool")
                                            .queryParam("borough", "Manhattan")
                                            .queryParam("cuisine", "Italian")
                                            .queryParam("name", "Restaurant")
                                            .queryParam("limit", 0)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenOffsetIsNegative_thenReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/must/bool")
                                            .queryParam("borough", "Manhattan")
                                            .queryParam("cuisine", "Italian")
                                            .queryParam("name", "Restaurant")
                                            .queryParam("offset", -1)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }
    }

    @Nested
    class AggregateSearchValidation {
        @Test
        void whenFieldNamesIsEmpty_thenBadRequest() {
            webTestClient
                    .get()
                    .uri("/search/aggregate?searchKeyword=test&fieldNames=")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenSearchKeywordIsBlank_thenBadRequest() {
            webTestClient
                    .get()
                    .uri("/search/aggregate?searchKeyword=&fieldNames=name")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenValidSearchParameters_thenOk() {
            given(
                            searchService.aggregateSearch(
                                    anyString(), anyList(), anyString(), anyInt(), anyInt(), any()))
                    .willReturn(
                            Mono.just(
                                    new AggregationSearchResponse(
                                            List.of(), Map.of(), null, 0, 0)));

            webTestClient
                    .get()
                    .uri("/search/aggregate?searchKeyword=test&fieldNames=name&limit=10&offset=0")
                    .exchange()
                    .expectStatus()
                    .isOk();
        }
    }

    @Nested
    class WithInRangeValidation {
        @Test
        void whenLatitudeExceeds90_thenBadRequest() {
            webTestClient
                    .get()
                    .uri("/search/restaurant/withInRange?lat=91&lon=0&distance=10&unit=km")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenLongitudeExceeds180_thenBadRequest() {
            webTestClient
                    .get()
                    .uri("/search/restaurant/withInRange?lat=0&lon=181&distance=10&unit=km")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenDistanceIsNegative_thenBadRequest() {
            webTestClient
                    .get()
                    .uri("/search/restaurant/withInRange?lat=0&lon=0&distance=-1&unit=km")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenUnitIsInvalid_thenBadRequest() {
            webTestClient
                    .get()
                    .uri("/search/restaurant/withInRange?lat=0&lon=0&distance=10&unit=invalid")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenAllParametersAreValid_thenOk() {
            given(searchService.searchRestaurantsWithInRange(40.7128, -74.0060, 10d, "km"))
                    .willReturn(Flux.empty());

            webTestClient
                    .get()
                    .uri(
                            "/search/restaurant/withInRange?lat=40.7128&lon=-74.0060&distance=10&unit=km")
                    .exchange()
                    .expectStatus()
                    .isOk();
        }
    }
}
