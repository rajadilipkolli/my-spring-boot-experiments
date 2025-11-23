package com.example.mongoes.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.data.geo.Point;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@WebFluxTest(RestaurantController.class)
class RestaurantControllerTest {

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private RestaurantService restaurantService;

    @Test
    void findAllRestaurants_WithValidLimit_ShouldReturnOk() {
        given(restaurantService.findAllRestaurants(0, 100)).willReturn(Mono.empty());

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
        given(restaurantService.findAllRestaurants(0, 10)).willReturn(Mono.empty());

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
                .isBadRequest()
                .expectHeader()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .json(
                        """
                        {
                   	"type": "https://api.mongoes.com/errors/validation-error",
                   	"title": "Constraint Violation",
                   	"status": 400,
                   	"detail": "Validation failed",
                   	"instance": "/api/restaurant",
                   	"violations": [
                   		{
                   			"object": "RestaurantController",
                   			"field": "findAllRestaurants.limit",
                   			"rejectedValue": 1000,
                   			"message": "must be less than or equal to 999"
                   		}
                   	]
                   }
                  """);
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
                .isBadRequest()
                .expectHeader()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .json(
                        """
                        {"type":"https://api.mongoes.com/errors/validation-error","title":"Bad Request","status":400,"detail":"Invalid request content.","instance":"/api/restaurant"}
                  """);
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
                .isBadRequest()
                .expectHeader()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .json(
                        """
                        {"type":"https://api.mongoes.com/errors/validation-error","title":"Bad Request","status":400,"detail":"Invalid request content.","instance":"/api/restaurant"}
                  """);
    }

    @Test
    void whenValidGrade_willReturns200() {
        // given
        Long restaurantId = 1L;
        GradesRequest validGrade = new GradesRequest("A", LocalDateTime.now(), 90);
        given(restaurantService.addGrade(validGrade, restaurantId))
                .willReturn(Mono.just(new Restaurant()));

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
    void whenInvalidGrade_willReturns400() {
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
                .isBadRequest()
                .expectHeader()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .json(
                        """
                        {"type":"https://api.mongoes.com/errors/validation-error","title":"Bad Request","status":400,"detail":"Invalid request content.","instance":"/api/restaurant/1/grade"}
                  """);
    }

    @Test
    void whenNegativeScore_willReturns400() {
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
                .isBadRequest()
                .expectHeader()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .json(
                        """
                        {"type":"https://api.mongoes.com/errors/validation-error","title":"Bad Request","status":400,"detail":"Invalid request content.","instance":"/api/restaurant/1/grade"}
                  """);
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

        given(restaurantService.findByRestaurantName(validName)).willReturn(Mono.just(restaurant));

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
                .exists()
                .jsonPath("$.address.street")
                .isEqualTo("Street")
                .jsonPath("$.address.building")
                .isEqualTo("Building")
                .jsonPath("$.address.zipcode")
                .isEqualTo(12345)
                .jsonPath("$.address.location.x")
                .isEqualTo(40.0)
                .jsonPath("$.address.location.y")
                .isEqualTo(-73.0);
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   "})
    void findRestaurantByName_WithEmptyName_ShouldReturnBadRequest(String emptyName) {
        webTestClient
                .get()
                .uri("/api/restaurant/name/{restaurantName}", emptyName)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectHeader()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void findRestaurantByName_WithTooLongName_ShouldReturnBadRequest() {
        String tooLongName = "a".repeat(256); // Create a string longer than 255 characters

        webTestClient
                .get()
                .uri("/api/restaurant/name/{restaurantName}", tooLongName)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectHeader()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .json(
                        """
                  {"type":"https://api.mongoes.com/errors/validation-error","title":"Constraint Violation","status":400,"detail":"Validation failed","instance":"/api/restaurant/name/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa","violations":[{"object":"RestaurantController","field":"findRestaurantByName.restaurantName","rejectedValue":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa","message":"size must be between 0 and 255"}]}
                  """);
    }

    @Test
    void findRestaurantByName_WithMaxLengthName_ShouldReturnOk() {
        String maxLengthName = "a".repeat(255);
        Restaurant restaurant = new Restaurant();
        restaurant.setName(maxLengthName);

        given(restaurantService.findByRestaurantName(maxLengthName))
                .willReturn(Mono.just(restaurant));

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
                .isEqualTo("https://api.mongoes.com/errors/validation-error")
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

        given(restaurantService.findByRestaurantName(validName))
                .willReturn(Mono.just(restaurant).delayElement(Duration.ofSeconds(3)));

        webTestClient
                .mutate()
                .responseTimeout(Duration.ofSeconds(6))
                .build()
                .get()
                .uri("/api/restaurant/name/{restaurantName}", validName)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.name")
                .isEqualTo(validName);
    }

    @Test
    void handleMultipleConcurrentRequests() {
        String validName = "Test Restaurant";
        Restaurant restaurant = new Restaurant();
        restaurant.setName(validName);

        given(restaurantService.findByRestaurantName(validName)).willReturn(Mono.just(restaurant));

        Flux<Restaurant> responseFlux =
                Flux.range(1, 10)
                        .flatMap(
                                i ->
                                        webTestClient
                                                .get()
                                                .uri(
                                                        "/api/restaurant/name/{restaurantName}",
                                                        validName)
                                                .exchange()
                                                .expectStatus()
                                                .isOk()
                                                .returnResult(Restaurant.class)
                                                .getResponseBody());

        StepVerifier.create(responseFlux)
                .expectNextCount(10)
                .expectComplete()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    void findRestaurantById_WithValidId_ShouldReturnOk() {
        Long validId = 1L;
        Restaurant restaurant = new Restaurant();
        restaurant.setId(String.valueOf(validId));

        given(restaurantService.findByRestaurantId(validId)).willReturn(Mono.just(restaurant));

        webTestClient
                .get()
                .uri("/api/restaurant/{restaurantId}", validId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id")
                .isEqualTo(validId);
    }

    @Test
    void createRestaurant_WithValidRequest_ShouldReturnCreated() {
        RestaurantRequest validRequest =
                new RestaurantRequest(
                        1L, "Test Restaurant", "borough", "cuisine", new Address(), List.of());
        Restaurant restaurant = new Restaurant();
        restaurant.setName(validRequest.name());

        given(restaurantService.createRestaurant(any(RestaurantRequest.class)))
                .willReturn(Mono.just(restaurant));

        webTestClient
                .post()
                .uri("/api/restaurant")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .valueEquals("Location", "/api/restaurant/name/Test+Restaurant");
    }

    @Test
    void createRestaurant_WithInvalidRequest_ShouldReturnBadRequest() {
        RestaurantRequest invalidRequest =
                new RestaurantRequest(1L, "", "borough", "cuisine", new Address(), List.of());

        webTestClient
                .post()
                .uri("/api/restaurant")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectHeader()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .json(
                        """
                    {"type":"https://api.mongoes.com/errors/validation-error","title":"Bad Request","status":400,"detail":"Invalid request content.","instance":"/api/restaurant"}
              """);
    }

    @Test
    void updateGradesOfRestaurant_WithValidRequest_ShouldReturnOk() {
        Long restaurantId = 1L;
        List<GradesRequest> validGrades = List.of(new GradesRequest("A", LocalDateTime.now(), 90));
        Restaurant restaurant = new Restaurant();
        restaurant.setId(String.valueOf(restaurantId));

        given(restaurantService.updateGrades(validGrades, restaurantId))
                .willReturn(Mono.just(restaurant));

        webTestClient
                .put()
                .uri("/api/restaurant/{restaurantId}/grades", restaurantId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validGrades)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void updateGradesOfRestaurant_WithInvalidRequest_ShouldReturnBadRequest() {
        Long restaurantId = 1L;
        List<GradesRequest> invalidGrades = List.of(new GradesRequest(null, null, null));

        webTestClient
                .put()
                .uri("/api/restaurant/{restaurantId}/grades", restaurantId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidGrades)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectHeader()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .json(
                        """
                        {"type":"https://api.mongoes.com/errors/validation-error","title":"Constraint Violation","status":400,"detail":"Validation failed","instance":"/api/restaurant/1/grades","violations":[{"object":"RestaurantController","field":"updateGradesOfRestaurant.grades[0].date","rejectedValue":null,"message":"Date cannot be null"},{"object":"RestaurantController","field":"updateGradesOfRestaurant.grades[0].grade","rejectedValue":null,"message":"Grade cannot be blank"},{"object":"RestaurantController","field":"updateGradesOfRestaurant.grades[0].score","rejectedValue":null,"message":"Score cannot be null"}]}
                        """);
    }

    @Test
    void totalCount_ShouldReturnOk() {
        given(restaurantService.totalCount()).willReturn(Mono.just(100L));

        webTestClient
                .get()
                .uri("/api/restaurant/total")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$")
                .isEqualTo(100L);
    }
}
