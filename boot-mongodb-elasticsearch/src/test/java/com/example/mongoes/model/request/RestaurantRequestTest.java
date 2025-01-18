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

        JsonContent<RestaurantRequest> json = jacksonTester.write(restaurantRequest);
        assertThat(json.getJson()).isNotNull();

        RestaurantRequest orderRequest = jacksonTester.parseObject(json.getJson());
        assertThat(orderRequest).isNotNull();
    }
}
