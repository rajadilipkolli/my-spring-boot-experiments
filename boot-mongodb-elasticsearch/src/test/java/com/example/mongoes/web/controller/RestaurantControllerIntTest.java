package com.example.mongoes.web.controller;

import com.example.mongoes.common.AbstractIntegrationTest;
import com.example.mongoes.document.Address;
import com.example.mongoes.web.model.GradesRequest;
import com.example.mongoes.web.model.RestaurantRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.geo.Point;
import org.springframework.http.MediaType;

class RestaurantControllerIntTest extends AbstractIntegrationTest {

    @Test
    void createRestaurant() {
        Address address = new Address();
        address.setLocation(new Point(-73.9, 40.8));
        GradesRequest grade = new GradesRequest("A", LocalDateTime.of(2022, 1, 1, 1, 1, 1), 15);
        GradesRequest grade1 =
                new GradesRequest("B", LocalDateTime.of(2022, 3, 31, 23, 59, 59), 15);
        RestaurantRequest restaurantRequest =
                new RestaurantRequest(
                        1L,
                        "junitRestaurant",
                        "junitBorough",
                        "junitCuisine",
                        address,
                        List.of(grade, grade1));
        this.webTestClient
                .post()
                .uri("/api/restaurant")
                .bodyValue(restaurantRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectHeader()
                .exists("location")
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("restaurant with name junitRestaurant created");
    }
}
