package com.example.mongoes.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.mongoes.common.AbstractIntegrationTest;
import com.example.mongoes.document.Address;
import com.example.mongoes.document.Grades;
import com.example.mongoes.document.Restaurant;
import com.example.mongoes.model.response.SearchPageResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.geo.Point;
import org.springframework.http.MediaType;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SearchControllerIntTest extends AbstractIntegrationTest {

    private static final String RESTAURANT_NAME = "Lb Spumoni Gardens";
    private static final String BOROUGH_NAME = "Brooklyn";
    private static final String CUISINE_NAME = "Pizza/Italian";

    @BeforeAll
    void setUpData() {
        Restaurant restaurant = createRestaurant(2L, RESTAURANT_NAME, BOROUGH_NAME, CUISINE_NAME);
        Restaurant restaurant1 = createRestaurant(40363920L, "Yono gardens", "Brooklyn", "Chinese");

        this.restaurantESRepository
                .deleteAll()
                .log()
                .thenMany(this.restaurantESRepository.saveAll(List.of(restaurant, restaurant1)))
                .log("saving restaurant")
                .subscribe();

        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(
                        () -> assertThat(this.restaurantESRepository.count().block()).isEqualTo(2));
    }

    private Restaurant createRestaurant(long id, String name, String borough, String cuisine) {
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(id);
        restaurant.setName(name);
        restaurant.setBorough(borough);
        restaurant.setCuisine(cuisine);
        Address address =
                new Address()
                        .setLocation(new Point(-73.9, 40.8))
                        .setBuilding("junitBuilding")
                        .setZipcode(98765)
                        .setStreet("junitStreet");
        restaurant.setAddress(address);
        Grades grade = new Grades("A", LocalDateTime.of(2024, 1, 1, 1, 1, 1), 15);
        Grades grade1 = new Grades("B", LocalDateTime.of(2025, 3, 31, 23, 59, 59), 15);
        restaurant.setGrades(List.of(grade, grade1));
        return restaurant;
    }

    @Test
    void searchBorough() {

        webTestClient
                .get()
                .uri("/search/borough?query=manhattan&limit=5&offset=0")
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void searchMulti_ShouldReturnSearchPage() {

        webTestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/search/multi")
                                        .queryParam("query", "gardens")
                                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<SearchPageResponse<Restaurant>>() {})
                .value(page -> assertThat(page.totalElements()).isEqualTo(1L));
    }

    @Test
    void searchTermForBorough_ShouldReturnSearchPage() {

        webTestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/search/term/borough")
                                        .queryParam("query", BOROUGH_NAME)
                                        .queryParam("offset", 0)
                                        .queryParam("limit", 10)
                                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<SearchPageResponse<Restaurant>>() {})
                .value(page -> assertThat(page.totalElements()).isEqualTo(1L));
    }

    @Test
    void searchTerms_ShouldReturnSearchPage() {

        webTestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/search/terms")
                                        .queryParam("query", "Brooklyn")
                                        .queryParam("offset", 0)
                                        .queryParam("limit", 10)
                                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<SearchPageResponse<Restaurant>>() {})
                .value(page -> assertThat(page.totalElements()).isEqualTo(1L));
    }

    @Test
    void searchBoolMust_ShouldReturnMatchingRestaurants() {

        webTestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/search/must/bool")
                                        .queryParam("name", RESTAURANT_NAME)
                                        .queryParam("borough", BOROUGH_NAME)
                                        .queryParam("cuisine", CUISINE_NAME)
                                        .queryParam("offset", 0)
                                        .queryParam("limit", 10)
                                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Restaurant.class)
                .hasSize(1)
                .value(
                        restaurants -> {
                            Restaurant restaurant = restaurants.getFirst();
                            assertThat(restaurant).isNotNull();
                            assertThat(restaurant.getId()).isNotBlank();
                            assertThat(restaurant.getRestaurantId()).isEqualTo(2);
                            assertThat(restaurant.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant.getAddress()).isNotNull();
                            assertThat(restaurant.getAddress().getBuilding())
                                    .isNotNull()
                                    .isEqualTo("junitBuilding");
                            assertThat(restaurant.getAddress().getLocation())
                                    .isNotNull()
                                    .isEqualTo(new Point(-73.9, 40.8));
                            assertThat(restaurant.getAddress().getStreet())
                                    .isNotNull()
                                    .isEqualTo("junitStreet");
                            assertThat(restaurant.getAddress().getZipcode())
                                    .isNotNull()
                                    .isEqualTo(98765);
                            assertThat(restaurant.getGrades()).isNotNull().isNotEmpty().hasSize(2);
                            assertThat(restaurant.getGrades().getFirst().getGrade()).isEqualTo("A");
                            assertThat(restaurant.getGrades().getFirst().getDate())
                                    .isEqualTo("2024-01-01T01:01:01");
                            assertThat(restaurant.getGrades().getFirst().getScore()).isEqualTo(15);
                        });
    }

    @Test
    void searchBoolShould_ShouldReturnSearchPage() {

        webTestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/search/should/bool")
                                        .queryParam("borough", BOROUGH_NAME)
                                        .queryParam("cuisine", CUISINE_NAME)
                                        .queryParam("name", RESTAURANT_NAME)
                                        .queryParam("offset", 0)
                                        .queryParam("limit", 5)
                                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<SearchPageResponse<Restaurant>>() {})
                .value(page -> assertThat(page.totalElements()).isEqualTo(1L));
    }

    @Test
    void searchWildcard_ShouldReturnSearchPage() {

        webTestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/search/wildcard")
                                        .queryParam("query", "garden")
                                        .queryParam("offset", 0)
                                        .queryParam("limit", 10)
                                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<SearchPageResponse<Restaurant>>() {})
                .value(page -> assertThat(page.totalElements()).isEqualTo(1L));
    }

    @Test
    void searchRegularExpression_ShouldReturnSearchPage() {

        webTestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/search/regexp/borough")
                                        .queryParam("query", "Test.*")
                                        .queryParam("offset", 0)
                                        .queryParam("limit", 10)
                                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<SearchPageResponse<Restaurant>>() {})
                .value(page -> assertThat(page.totalElements()).isEqualTo(0L));
    }

    @Test
    void searchSimpleQuery_ShouldReturnSearchPage() {

        webTestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/search/simple")
                                        .queryParam("query", "Manhattan")
                                        .queryParam("offset", 0)
                                        .queryParam("limit", 10)
                                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<SearchPageResponse<Restaurant>>() {})
                .value(page -> assertThat(page.totalElements()).isEqualTo(1L));
    }

    @Test
    void searchRestaurantIdRange_ShouldReturnSearchPage() {

        webTestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/search/restaurant/range")
                                        .queryParam("lowerLimit", 1)
                                        .queryParam("upperLimit", 100)
                                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<SearchPageResponse<Restaurant>>() {})
                .value(page -> assertThat(page.totalElements()).isEqualTo(1L));
    }

    @Test
    void searchDateRange_ShouldReturnSearchPage() {

        webTestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/search/date/range")
                                        .queryParam("fromDate", "2024-01-01")
                                        .queryParam("toDate", "2024-12-31")
                                        .queryParam("offset", 0)
                                        .queryParam("limit", 10)
                                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<SearchPageResponse<Restaurant>>() {})
                .value(page -> assertThat(page.totalElements()).isEqualTo(1L));
    }

    @Test
    void aggregateSearch_ShouldReturnAggregationResponse() {

        webTestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/search/aggregate")
                                        .queryParam("searchKeyword", "garden")
                                        .queryParam("fieldNames", "name", "cuisine")
                                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<SearchPageResponse<Restaurant>>() {});
    }
}
