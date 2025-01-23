package com.example.mongoes.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.mongoes.document.Address;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.context.annotation.Import;
import org.springframework.data.geo.Point;
import org.springframework.data.web.config.SpringDataJacksonConfiguration;

@JsonTest
@Import(SpringDataJacksonConfiguration.class)
class RestaurantRequestTest {

    @Autowired private JacksonTester<RestaurantRequest> jacksonTester;

    @Test
    void serializeInCorrectFormat() throws IOException {
        Address address = new Address();
        address.setLocation(new Point(-73.9, 40.8));
        RestaurantRequest restaurantRequest = getRestaurantRequest(address);

        JsonContent<RestaurantRequest> json = jacksonTester.write(restaurantRequest);
        assertThat(json.getJson()).isNotNull();

        RestaurantRequest orderRequest = jacksonTester.parseObject(json.getJson());
        assertThat(orderRequest).isNotNull();
    }

    @Test
    void deserializeInCorrectFormat() throws IOException {
        String jsonContent =
                """
        {
            "restaurantId": 1,
            "name": "junitRestaurant",
            "borough": "junitBorough",
            "cuisine": "junitCuisine",
            "address": {
                "location": {
                    "x": -73.9,
                    "y": 40.8
                }
            },
            "grades": [
                {
                    "grade": "A",
                    "date": "2022-01-01T01:01:01",
                    "score": 15
                },
                {
                    "grade": "B",
                    "date": "2022-03-31T23:59:59",
                    "score": 15
                }
            ]
        }
        """;

        RestaurantRequest restaurantRequest = jacksonTester.parseObject(jsonContent);
        assertThat(restaurantRequest).isNotNull();
        assertThat(restaurantRequest.restaurantId()).isEqualTo(1L);
        assertThat(restaurantRequest.name()).isEqualTo("junitRestaurant");
        assertThat(restaurantRequest.borough()).isEqualTo("junitBorough");
        assertThat(restaurantRequest.cuisine()).isEqualTo("junitCuisine");
        assertThat(restaurantRequest.address().getLocation().getX()).isEqualTo(-73.9);
        assertThat(restaurantRequest.address().getLocation().getY()).isEqualTo(40.8);
        assertThat(restaurantRequest.grades()).hasSize(2);
        assertThat(restaurantRequest.grades().getFirst().grade()).isEqualTo("A");
        assertThat(restaurantRequest.grades().get(0).date())
                .isEqualTo(LocalDateTime.of(2022, 1, 1, 1, 1, 1));
        assertThat(restaurantRequest.grades().get(0).score()).isEqualTo(15);
        assertThat(restaurantRequest.grades().get(1).grade()).isEqualTo("B");
        assertThat(restaurantRequest.grades().get(1).date())
                .isEqualTo(LocalDateTime.of(2022, 3, 31, 23, 59, 59));
        assertThat(restaurantRequest.grades().get(1).score()).isEqualTo(15);
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
