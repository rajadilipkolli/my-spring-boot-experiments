package com.example.mongoes.web.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.mongoes.document.Address;
import com.example.mongoes.document.Restaurant;
import com.example.mongoes.web.model.GradesRequest;
import com.example.mongoes.web.model.RestaurantRequest;
import com.example.mongoes.web.service.RestaurantService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(RestaurantController.class)
class RestaurantControllerTest {

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private RestaurantService restaurantService;

    @Test
    void findAllRestaurants_WithValidLimit_ShouldReturnOk() {
        when(restaurantService.findAllRestaurants(0, 100)).thenReturn(Mono.empty());

        webTestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/api/restaurant")
                                        .queryParam("limit", "100")
                                        .build())
                .exchange()
                .expectStatus()
                .isOk();

        verify(restaurantService).findAllRestaurants(0, 100);
    }

    @Test
    void findAllRestaurants_WithDefaultLimit_ShouldReturnOk() {
        when(restaurantService.findAllRestaurants(0, 10)).thenReturn(Mono.empty());

        webTestClient.get().uri("/api/restaurant").exchange().expectStatus().isOk();
    }

    @Test
    void findAllRestaurants_WithInvalidLimit_ShouldReturnBadRequest() {
        webTestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/api/restaurant")
                                        .queryParam("limit", "1000")
                                        .build())
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void whenRestaurantRequestWithNullName_thenBadRequest() {
        RestaurantRequest invalidRequest =
                new RestaurantRequest(1L, null, "borough", "cuisine", new Address(), List.of());

        this.webTestClient
                .post()
                .uri("/api/restaurant")
                .bodyValue(invalidRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void whenRestaurantRequestWithEmptyBorough_thenBadRequest() {
        RestaurantRequest invalidRequest =
                new RestaurantRequest(1L, "name", "", "cuisine", new Address(), List.of());

        this.webTestClient
                .post()
                .uri("/api/restaurant")
                .bodyValue(invalidRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void whenValidGrade_thenReturns200() {
        // given
        Long restaurantId = 1L;
        GradesRequest validGrade = new GradesRequest("A", LocalDateTime.now(), 90);
        when(restaurantService.addGrade(validGrade, restaurantId))
                .thenReturn(Mono.just(new Restaurant()));

        // when/then
        webTestClient
                .post()
                .uri("/api/restaurant/{restaurantId}/grade", restaurantId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validGrade)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void whenInvalidGrade_thenReturns400() {
        // given
        Long restaurantId = 1L;
        // grade is required but not set
        GradesRequest invalidGrade = new GradesRequest(null, null, null);

        // when/then
        webTestClient
                .post()
                .uri("/api/restaurant/{restaurantId}/grade", restaurantId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidGrade)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void whenNegativeScore_thenReturns400() {
        // given
        Long restaurantId = 1L;
        GradesRequest invalidGrade = new GradesRequest("A", LocalDateTime.now(), -1);

        // when/then
        webTestClient
                .post()
                .uri("/api/restaurant/{restaurantId}/grade", restaurantId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidGrade)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }
}
