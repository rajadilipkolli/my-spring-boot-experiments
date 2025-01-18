package com.example.mongoes.web.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.mongoes.document.Address;
import com.example.mongoes.document.Restaurant;
import com.example.mongoes.model.request.GradesRequest;
import com.example.mongoes.model.request.RestaurantRequest;
import com.example.mongoes.web.service.RestaurantService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.data.geo.Point;
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

    @Test
    void findRestaurantByName_WithValidName_ShouldReturnOk() {
        String validName = "Test Restaurant";
        Restaurant restaurant = new Restaurant();
        restaurant.setName(validName);
        restaurant.setBorough("Manhattan");
        restaurant.setCuisine("Italian");
        restaurant.setAddress(
                new Address()
                        .setStreet("Street")
                        .setBuilding("Building")
                        .setZipcode(12345)
                        .setLocation(new Point(40.0, -73.0)));

        when(restaurantService.findByRestaurantName(validName)).thenReturn(Mono.just(restaurant));

        webTestClient
                .get()
                .uri("/api/restaurant/name/{restaurantName}", validName)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name")
                .isEqualTo(validName)
                .jsonPath("$.borough")
                .isEqualTo("Manhattan")
                .jsonPath("$.cuisine")
                .isEqualTo("Italian")
                .jsonPath("$.address")
                .exists();
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   "})
    void findRestaurantByName_WithEmptyName_ShouldReturnBadRequest(String emptyName) {
        webTestClient
                .get()
                .uri("/api/restaurant/name/{restaurantName}", emptyName)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void findRestaurantByName_WithTooLongName_ShouldReturnBadRequest() {
        String tooLongName = "a".repeat(256); // Create a string longer than 255 characters

        webTestClient
                .get()
                .uri("/api/restaurant/name/{restaurantName}", tooLongName)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void findRestaurantByName_WithMaxLengthName_ShouldReturnOk() {
        String maxLengthName = "a".repeat(255);
        Restaurant restaurant = new Restaurant();
        restaurant.setName(maxLengthName);

        when(restaurantService.findByRestaurantName(maxLengthName))
                .thenReturn(Mono.just(restaurant));

        webTestClient
                .get()
                .uri("/api/restaurant/name/{restaurantName}", maxLengthName)
                .exchange()
                .expectStatus()
                .isOk();
    }

    /** Restaurant names should only contain letters, numbers, spaces, and hyphens. */
    @ParameterizedTest
    @ValueSource(strings = {"Test@Restaurant", "Restaurant#123", "Restaurant$", "Restaurant&Co"})
    void findRestaurantByName_WithInvalidCharacters_ShouldReturnBadRequest(
            String nameWithInvalidChars) {

        webTestClient
                .get()
                .uri("/api/restaurant/name/{restaurantName}", nameWithInvalidChars)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo("Constraint Violation")
                .jsonPath("$.status")
                .isEqualTo(400)
                .jsonPath("$.detail")
                .isEqualTo("Validation failed")
                .jsonPath("$.instance")
                .isEqualTo(
                        "/api/restaurant/name/"
                                + URLEncoder.encode(nameWithInvalidChars, StandardCharsets.UTF_8))
                .jsonPath("$.violations[0].object")
                .isEqualTo("RestaurantController")
                .jsonPath("$.violations[0].field")
                .isEqualTo("findRestaurantByName.restaurantName")
                .jsonPath("$.violations[0].rejectedValue")
                .isEqualTo(nameWithInvalidChars)
                .jsonPath("$.violations[0].message")
                .isEqualTo("must match \"^[a-zA-Z0-9 .-]+$\"");
    }

    @Test
    void findRestaurantByName_WithSlowResponse_ShouldEventuallyComplete() {
        String validName = "Test Restaurant";
        Restaurant restaurant = new Restaurant();
        restaurant.setName(validName);

        when(restaurantService.findByRestaurantName(validName))
                .thenReturn(Mono.just(restaurant).delayElement(Duration.ofSeconds(1)));

        webTestClient
                .get()
                .uri("/api/restaurant/name/{restaurantName}", validName)
                .exchange()
                .expectStatus()
                .isOk();
    }
}
