package com.example.mongoes.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.mongoes.common.AbstractIntegrationTest;
import com.example.mongoes.document.Address;
import com.example.mongoes.model.request.GradesRequest;
import com.example.mongoes.model.request.RestaurantRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.data.geo.Point;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.test.StepVerifier;

class RestaurantControllerIntTest extends AbstractIntegrationTest {

    @BeforeEach
    void tearDown() {
        StepVerifier.create(restaurantESRepository.deleteAll())
                .expectComplete()
                .verify(Duration.ofSeconds(3));
    }

    @Test
    void createRestaurant() {
        Address address = new Address();
        address.setLocation(new Point(-73.9, 40.8));
        RestaurantRequest restaurantRequest = getRestaurantRequest(address);
        this.webTestClient
                .post()
                .uri("/api/restaurant")
                .bodyValue(restaurantRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .exists(HttpHeaders.LOCATION);
    }

    @Test
    void findAllRestaurants_ShouldReturnPagedResults() {
        // First create a restaurant
        Address address = new Address();
        address.setLocation(new Point(-73.9, 40.8));
        RestaurantRequest restaurantRequest = getRestaurantRequest(address);

        this.webTestClient
                .post()
                .uri("/api/restaurant")
                .bodyValue(restaurantRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated();

        // Then fetch all restaurants
        this.webTestClient
                .get()
                .uri("/api/restaurant?limit=10")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.content")
                .isArray()
                .jsonPath("$.totalElements")
                .isNumber();
    }

    @Test
    @Disabled
    void findRestaurantByName_WithExistingName_ShouldReturnRestaurant() {
        // First create a restaurant
        Address address = new Address();
        address.setLocation(new Point(-73.9, 40.8));
        RestaurantRequest restaurantRequest = getRestaurantRequest(address);

        this.webTestClient
                .post()
                .uri("/api/restaurant")
                .bodyValue(restaurantRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated();

        // Then fetch by name
        this.webTestClient
                .get()
                .uri("/api/restaurant/name/{restaurantName}", "junitRestaurant")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.name")
                .isEqualTo("junitRestaurant")
                .jsonPath("$.borough")
                .isEqualTo("junitBorough")
                .jsonPath("$.cuisine")
                .isEqualTo("junitCuisine");
    }

    @Test
    @Disabled
    void findRestaurantById_WithExistingId_ShouldReturnRestaurant() {
        // First create a restaurant
        Address address = new Address();
        address.setLocation(new Point(-73.9, 40.8));
        RestaurantRequest restaurantRequest = getRestaurantRequest(address);

        String locationHeader =
                this.webTestClient
                        .post()
                        .uri("/api/restaurant")
                        .bodyValue(restaurantRequest)
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus()
                        .isCreated()
                        .returnResult(Void.class)
                        .getResponseHeaders()
                        .getFirst(HttpHeaders.LOCATION);

        // Extract ID from location header
        String restaurantName = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);

        // Then fetch by name
        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(
                        () -> {
                            this.webTestClient
                                    .get()
                                    .uri("/api/restaurant/name/{restaurantName}", restaurantName)
                                    .exchange()
                                    .expectStatus()
                                    .isOk()
                                    .expectBody()
                                    .jsonPath("$.name")
                                    .isEqualTo("restaurantName");
                        });
    }

    @Test
    @Disabled
    void addGradeToRestaurant_ShouldAddNewGrade() {
        // First create a restaurant
        Address address = new Address();
        address.setLocation(new Point(-73.9, 40.8));
        RestaurantRequest restaurantRequest = getRestaurantRequest(address);

        String locationHeader =
                this.webTestClient
                        .post()
                        .uri("/api/restaurant")
                        .bodyValue(restaurantRequest)
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus()
                        .isCreated()
                        .returnResult(Void.class)
                        .getResponseHeaders()
                        .getFirst(HttpHeaders.LOCATION);

        String restaurantName = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);

        // Then add a new grade
        GradesRequest newGrade = new GradesRequest("C", LocalDateTime.now(), 10);

        this.webTestClient
                .post()
                .uri("/api/restaurant/{restaurantId}/grade", restaurantName)
                .bodyValue(newGrade)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.grades[2].grade")
                .isEqualTo("C")
                .jsonPath("$.grades[2].score")
                .isEqualTo(10);
    }

    @Test
    @Disabled
    void totalCount_ShouldReturnNumberOfRestaurants() {
        // First create a restaurant
        Address address = new Address();
        address.setLocation(new Point(-73.9, 40.8));
        RestaurantRequest restaurantRequest = getRestaurantRequest(address);

        this.webTestClient
                .post()
                .uri("/api/restaurant")
                .bodyValue(restaurantRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated();

        // Then get total count
        this.webTestClient
                .get()
                .uri("/api/restaurant/total")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Long.class)
                .value(count -> assertThat(count).isPositive());
    }

    @Test
    @Disabled
    void addNotesToRestaurant_ShouldUpdateGrades() {
        // First create a restaurant
        Address address = new Address();
        address.setLocation(new Point(-73.9, 40.8));
        RestaurantRequest restaurantRequest = getRestaurantRequest(address);

        String locationHeader =
                this.webTestClient
                        .post()
                        .uri("/api/restaurant")
                        .bodyValue(restaurantRequest)
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus()
                        .isCreated()
                        .returnResult(Void.class)
                        .getResponseHeaders()
                        .getFirst(HttpHeaders.LOCATION);

        String restaurantId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);

        // Then update grades
        GradesRequest updatedGrade = new GradesRequest("A+", LocalDateTime.now(), 20);

        this.webTestClient
                .put()
                .uri("/api/restaurant/{restaurantId}/grades/", restaurantId)
                .bodyValue(updatedGrade)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.grades[-1].grade")
                .isEqualTo("A+")
                .jsonPath("$.grades[-1].score")
                .isEqualTo(20);
    }

    @Test
    void findRestaurantById_WithNonExistentId_ShouldReturnNotFound() {
        this.webTestClient
                .get()
                .uri("/api/restaurant/{restaurantId}", 999999L)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .json(
                        """
                        {
                        "type":"about:blank",
                        "title":"Not Found",
                        "status":404,
                        "detail":"Restaurant not found with id: 999999",
                        "instance":"/api/restaurant/999999"
                        }
                        """);
    }

    @Test
    void findRestaurantByName_WithNonExistentName_ShouldReturnNotFound() {
        String nonExistentName = "Non Existent Restaurant";
        webTestClient
                .get()
                .uri("/api/restaurant/name/{restaurantName}", nonExistentName)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .json(
                        """
                        {
                            "type":"about:blank",
                            "title":"Not Found",
                            "status":404,
                            "detail":"Restaurant not found with name: Non Existent Restaurant",
                            "instance":"/api/restaurant/name/Non%20Existent%20Restaurant"
                        }
                        """);
    }

    private RestaurantRequest getRestaurantRequest(Address address) {
        GradesRequest grade = new GradesRequest("A", LocalDateTime.of(2022, 1, 1, 1, 1, 1), 15);
        GradesRequest grade1 =
                new GradesRequest("B", LocalDateTime.of(2022, 3, 31, 23, 59, 59), 15);
        return new RestaurantRequest(
                1L,
                "junitRestaurant",
                "junitBorough",
                "junitCuisine",
                address,
                List.of(grade, grade1));
    }
}
