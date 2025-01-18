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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.data.geo.Point;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RestaurantControllerIntTest extends AbstractIntegrationTest {

    @Test
    @Order(101)
    void findAllRestaurants_ShouldReturnPagedResults() {

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
    @Order(1)
    void createRestaurant() {
        Address address =
                new Address()
                        .setBuilding("junitBuilding")
                        .setStreet("junitStreet")
                        .setZipcode(98765);
        address.setLocation(new Point(-73.9, 40.8));
        RestaurantRequest restaurantRequest =
                getRestaurantRequest(address, "junitRestaurant", 101L);
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
    @Order(2)
    void findRestaurantByName_WithExistingName_ShouldReturnRestaurant() {

        // Then fetch by name
        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(
                        () ->
                                this.webTestClient
                                        .get()
                                        .uri(
                                                "/api/restaurant/name/{restaurantName}",
                                                "junitRestaurant")
                                        .exchange()
                                        .expectStatus()
                                        .isOk()
                                        .expectBody()
                                        .jsonPath("$.id")
                                        .exists()
                                        .jsonPath("$.restaurantId")
                                        .isEqualTo(101)
                                        .jsonPath("$.name")
                                        .isEqualTo("junitRestaurant")
                                        .jsonPath("$.borough")
                                        .isEqualTo("junitBorough")
                                        .jsonPath("$.cuisine")
                                        .isEqualTo("junitCuisine")
                                        .jsonPath("$.version")
                                        .isEqualTo(0)
                                        .jsonPath("$.address.building")
                                        .isEqualTo("junitBuilding")
                                        .jsonPath("$.address.street")
                                        .isEqualTo("junitStreet")
                                        .jsonPath("$.address.zipcode")
                                        .isEqualTo(98765)
                                        .jsonPath("$.address.location.x")
                                        .isEqualTo(-73.9)
                                        .jsonPath("$.address.location.y")
                                        .isEqualTo(40.8)
                                        .jsonPath("$.grades")
                                        .isArray()
                                        .jsonPath("$.grades.size()")
                                        .isEqualTo(2));
    }

    @Test
    @Order(3)
    void findRestaurantById_WithExistingId_ShouldReturnRestaurant() {
        // First create a restaurant
        Address address = new Address();
        address.setLocation(new Point(-73.9, 40.8));
        RestaurantRequest restaurantRequest = getRestaurantRequest(address, "newName", 1000L);

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
                .pollInterval(Duration.ofMillis(500))
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
                                    .isEqualTo(restaurantName);
                        });
    }

    @Test
    @Order(4)
    void totalCount_ShouldReturnNumberOfRestaurants() {

        // Then get total count
        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(
                        () ->
                                this.webTestClient
                                        .get()
                                        .uri("/api/restaurant/total")
                                        .exchange()
                                        .expectStatus()
                                        .isOk()
                                        .expectBody(Long.class)
                                        .value(
                                                count ->
                                                        assertThat(count)
                                                                .isGreaterThanOrEqualTo(2)));
    }

    @Test
    @Order(11)
    void addGradeToRestaurant_ShouldAddNewGrade() {

        // Then add a new grade
        GradesRequest newGrade = new GradesRequest("C", LocalDateTime.now(), 10);

        this.webTestClient
                .post()
                .uri("/api/restaurant/{restaurantId}/grade", 101)
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
    @Order(12)
    @Disabled
    void addNotesToRestaurant_ShouldUpdateGrades() {

        // Then update grades
        GradesRequest updatedGrade = new GradesRequest("A+", LocalDateTime.now(), 20);

        this.webTestClient
                .put()
                .uri("/api/restaurant/{restaurantId}/grades/", 1000)
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

    private RestaurantRequest getRestaurantRequest(
            Address address, String restaurantName, Long restaurantId) {
        GradesRequest grade = new GradesRequest("A", LocalDateTime.of(2022, 1, 1, 1, 1, 1), 15);
        GradesRequest grade1 =
                new GradesRequest("B", LocalDateTime.of(2022, 3, 31, 23, 59, 59), 15);
        return new RestaurantRequest(
                restaurantId,
                restaurantName,
                "junitBorough",
                "junitCuisine",
                address,
                List.of(grade, grade1));
    }
}
