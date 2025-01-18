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
import org.junit.jupiter.api.Test;
import org.springframework.data.geo.Point;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class RestaurantControllerIntTest extends AbstractIntegrationTest {

    @Test
    void findAllRestaurants_ShouldReturnPagedResults() {

        // Setup test data
        createSampleRestaurants(1L);

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
    void createRestaurant() {
        // Setup test data
        createSampleRestaurants(2L);

        RestaurantRequest restaurantRequest = getRestaurantRequest(null, "Restaurant" + 2, 2L);
        this.webTestClient
                .post()
                .uri("/api/restaurant")
                .bodyValue(restaurantRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isEqualTo(409)
                .expectHeader()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .json(
                        """
                        {
                        "type":"about:blank",
                        "title":"Conflict",
                        "status":409,
                        "detail":"Restaurant with name Restaurant2 already exists",
                        "instance":"/api/restaurant"
                        }
                        """);
    }

    @Test
    void findRestaurantByName_WithExistingName_ShouldReturnRestaurant() {

        // Setup: Create a restaurant first
        Address address =
                new Address().setBuilding("testBuilding").setStreet("testStreet").setZipcode(98765);
        address.setLocation(new Point(-73.8, 40.1));
        RestaurantRequest restaurantRequest = getRestaurantRequest(address, "testRestaurant", 101L);

        createRestaurantAndWaitForIndex(restaurantRequest);

        // Then fetch by name
        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(
                        () ->
                                this.webTestClient
                                        .get()
                                        .uri(
                                                "/api/restaurant/name/{restaurantName}",
                                                "testRestaurant")
                                        .exchange()
                                        .expectStatus()
                                        .isOk()
                                        .expectBody()
                                        .jsonPath("$.id")
                                        .exists()
                                        .jsonPath("$.restaurantId")
                                        .isEqualTo(101)
                                        .jsonPath("$.name")
                                        .isEqualTo("testRestaurant")
                                        .jsonPath("$.borough")
                                        .isEqualTo("junitBorough")
                                        .jsonPath("$.cuisine")
                                        .isEqualTo("junitCuisine")
                                        .jsonPath("$.version")
                                        .isEqualTo(0)
                                        .jsonPath("$.address.building")
                                        .isEqualTo("testBuilding")
                                        .jsonPath("$.address.street")
                                        .isEqualTo("testStreet")
                                        .jsonPath("$.address.zipcode")
                                        .isEqualTo(98765)
                                        .jsonPath("$.address.location.x")
                                        .isEqualTo(-73.8)
                                        .jsonPath("$.address.location.y")
                                        .isEqualTo(40.1)
                                        .jsonPath("$.grades")
                                        .isArray()
                                        .jsonPath("$.grades.size()")
                                        .isEqualTo(2));
    }

    @Test
    void totalCount_ShouldReturnNumberOfRestaurants() {

        // Setup: Create multiple restaurants
        createSampleRestaurants(3L);

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
    void addGradeToRestaurant_ShouldAddNewGrade() {
        // Setup: Create multiple restaurants
        createSampleRestaurants(4L);
        // Then add a new grade
        GradesRequest newGrade = new GradesRequest("C", LocalDateTime.now(), 10);

        this.webTestClient
                .post()
                .uri("/api/restaurant/{restaurantId}/grade", 1)
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
    void addNotesToRestaurant_ShouldUpdateGrades() {

        // Setup: Create multiple restaurants
        createSampleRestaurants(5L);

        // Then update grades
        GradesRequest updatedGrade = new GradesRequest("B", LocalDateTime.now(), 20);

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

    private void createSampleRestaurants(Long restaurantId) {
        Address address1 =
                new Address().setBuilding("building1").setStreet("street1").setZipcode(12345);
        address1.setLocation(new Point(-73.9, 40.8));

        Address address2 =
                new Address().setBuilding("building2").setStreet("street2").setZipcode(67890);
        address2.setLocation(new Point(-73.8, 40.7));

        RestaurantRequest restaurant1 =
                getRestaurantRequest(address1, "Restaurant" + restaurantId, restaurantId);
        RestaurantRequest restaurant2 =
                getRestaurantRequest(
                        address2, "Restaurant" + restaurantId + 10000, restaurantId + 10000);

        createRestaurantAndWaitForIndex(restaurant1);
        createRestaurantAndWaitForIndex(restaurant2);
    }

    private void createRestaurantAndWaitForIndex(RestaurantRequest request) {
        this.webTestClient
                .post()
                .uri("/api/restaurant")
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .exists(HttpHeaders.LOCATION);

        // Wait for Elasticsearch indexing
        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(
                        () ->
                                this.webTestClient
                                        .get()
                                        .uri(
                                                "/api/restaurant/{restaurantId}",
                                                request.restaurantId())
                                        .exchange()
                                        .expectStatus()
                                        .isOk());
    }
}
