package com.example.mongoes.web.controller;

import com.example.mongoes.common.AbstractIntegrationTest;
import com.example.mongoes.document.Address;
import com.example.mongoes.model.request.GradesRequest;
import com.example.mongoes.model.request.RestaurantRequest;
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
                .contentType(MediaType.APPLICATION_JSON)
                .expectHeader()
                .exists("location")
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("restaurant with name junitRestaurant created");
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
