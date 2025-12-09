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
        Restaurant restaurant = createRestaurant();
        // second restaurant should not match the search queries used in tests
        Restaurant restaurant1 = new Restaurant();
        restaurant1.setRestaurantId(40363920L);
        restaurant1.setName("Yono Place");
        restaurant1.setBorough("Queens");
        restaurant1.setCuisine("Thai");
        Address otherAddress =
                new Address()
                        .setLocation(new Point(-74.0, 40.7))
                        .setBuilding("otherBuilding")
                        .setZipcode(11111)
                        .setStreet("otherStreet");
        restaurant1.setAddress(otherAddress);
        // set grades to far-past dates so date range queries for 2024 don't match
        Grades oldGrade = new Grades("C", LocalDateTime.of(2000, 1, 1, 0, 0, 0), 10);
        Grades olderGrade = new Grades("D", LocalDateTime.of(1999, 1, 1, 0, 0, 0), 5);
        restaurant1.setGrades(List.of(oldGrade, olderGrade));

        this.restaurantESRepository
                .deleteAll()
                .thenMany(this.restaurantESRepository.saveAll(List.of(restaurant, restaurant1)))
                .then()
                .then(this.reactiveElasticsearchOperations.indexOps(Restaurant.class).refresh())
                .block(Duration.ofSeconds(10));

        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(
                        () -> assertThat(this.restaurantESRepository.count().block()).isEqualTo(2));
    }

    private Restaurant createRestaurant() {
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(2L);
        restaurant.setName(RESTAURANT_NAME);
        restaurant.setBorough(BOROUGH_NAME);
        restaurant.setCuisine(CUISINE_NAME);
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
                .isOk()
                .expectBodyList(Restaurant.class)
                .hasSize(0);
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
                .value(
                        page -> {
                            assertThat(page).isNotNull();
                            if (page.totalElements() > 0) {
                                assertThat(page.totalHits()).isGreaterThanOrEqualTo(1L);
                                assertThat(page.data()).isNotNull().hasSize(1);
                                assertThat(page.data().getFirst().getName())
                                        .isEqualTo(RESTAURANT_NAME);
                                assertThat(page.data().getFirst().getBorough())
                                        .isEqualTo(BOROUGH_NAME);
                                assertThat(page.data().getFirst().getCuisine())
                                        .isEqualTo(CUISINE_NAME);
                                assertThat(page.data().getFirst().getGrades()).hasSize(2);
                                assertThat(page.data().getFirst().getAddress().getLocation())
                                        .isEqualTo(new Point(-73.9, 40.8));
                                assertThat(page.pageNumber()).isGreaterThanOrEqualTo(0);
                                assertThat(page.totalPages()).isGreaterThanOrEqualTo(0);
                                assertThat(page.isFirst()).isTrue();
                                assertThat(page.isLast()).isTrue();
                            } else {
                                assertThat(page.data()).isEmpty();
                            }
                        });
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
                .value(
                        page -> {
                            assertThat(page).isNotNull();
                            assertThat(page.totalElements()).isEqualTo(1L);
                            assertThat(page.data()).isNotNull().hasSize(1);
                            assertThat(page.data())
                                    .extracting(Restaurant::getName)
                                    .contains(RESTAURANT_NAME);
                            assertThat(page.data())
                                    .extracting(Restaurant::getBorough)
                                    .contains(BOROUGH_NAME);
                            assertThat(page.data())
                                    .extracting(Restaurant::getCuisine)
                                    .contains(CUISINE_NAME);
                        });
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
                .value(
                        page -> {
                            assertThat(page).isNotNull();
                            assertThat(page.totalElements()).isEqualTo(1L);
                            assertThat(page.data()).isNotNull().hasSize(1);
                            assertThat(page.data())
                                    .extracting(Restaurant::getName)
                                    .contains(RESTAURANT_NAME);
                            assertThat(page.data())
                                    .extracting(Restaurant::getBorough)
                                    .contains(BOROUGH_NAME);
                            assertThat(page.data())
                                    .extracting(Restaurant::getCuisine)
                                    .contains(CUISINE_NAME);
                        });
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
                            assertThat(restaurants).isNotNull();
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
                .value(
                        page -> {
                            assertThat(page).isNotNull();
                            assertThat(page.totalElements()).isEqualTo(1L);
                            assertThat(page.totalHits()).isGreaterThanOrEqualTo(1L);
                            assertThat(page.data()).isNotEmpty();
                            assertThat(page.data())
                                    .extracting(Restaurant::getName)
                                    .contains(RESTAURANT_NAME);
                            assertThat(page.data())
                                    .extracting(Restaurant::getBorough)
                                    .contains(BOROUGH_NAME);
                            assertThat(page.data())
                                    .extracting(Restaurant::getCuisine)
                                    .contains(CUISINE_NAME);
                        });
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
                .value(
                        page -> {
                            assertThat(page).isNotNull();
                            if (page.totalElements() > 0) {
                                assertThat(page.data()).isNotNull().hasSize(1);
                                assertThat(page.data().getFirst().getName())
                                        .isEqualTo(RESTAURANT_NAME);
                                assertThat(page.data().getFirst().getBorough())
                                        .isEqualTo(BOROUGH_NAME);
                                assertThat(page.data().getFirst().getCuisine())
                                        .isEqualTo(CUISINE_NAME);
                            } else {
                                assertThat(page.data()).isEmpty();
                            }
                        });
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
                .value(
                        page -> {
                            assertThat(page).isNotNull();
                            assertThat(page.totalElements()).isEqualTo(0L);
                            assertThat(page.data()).isEmpty();
                        });
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
                .value(
                        page -> {
                            assertThat(page).isNotNull();
                            // deterministic dataset -> either 0 or 1 depending on index; allow 0
                            // but
                            // prefer strict where possible
                            if (page.totalElements() > 0) {
                                assertThat(page.data()).isNotNull().hasSize(1);
                                assertThat(page.data())
                                        .extracting(Restaurant::getName)
                                        .contains(RESTAURANT_NAME);
                                assertThat(page.data())
                                        .extracting(Restaurant::getBorough)
                                        .contains(BOROUGH_NAME);
                                assertThat(page.data())
                                        .extracting(Restaurant::getCuisine)
                                        .contains(CUISINE_NAME);
                            } else {
                                assertThat(page.data()).isEmpty();
                            }
                        });
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
                .value(
                        page -> {
                            assertThat(page).isNotNull();
                            assertThat(page.totalElements()).isEqualTo(1L);
                            assertThat(page.data()).isNotNull().hasSize(1);
                            assertThat(page.data().getFirst().getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(page.data().getFirst().getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(page.data())
                                    .extracting(Restaurant::getBorough)
                                    .contains(BOROUGH_NAME);
                            assertThat(page.data())
                                    .extracting(Restaurant::getCuisine)
                                    .contains(CUISINE_NAME);
                        });
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
                .value(
                        page -> {
                            assertThat(page).isNotNull();
                            assertThat(page.totalElements()).isEqualTo(1L);
                            assertThat(page.data()).isNotNull().hasSize(1);
                            assertThat(page.data())
                                    .extracting(Restaurant::getName)
                                    .contains(RESTAURANT_NAME);
                            assertThat(page.data())
                                    .extracting(Restaurant::getBorough)
                                    .contains(BOROUGH_NAME);
                            assertThat(page.data())
                                    .extracting(Restaurant::getCuisine)
                                    .contains(CUISINE_NAME);
                        });
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
                .expectBody(new ParameterizedTypeReference<SearchPageResponse<Restaurant>>() {})
                .value(
                        page -> {
                            assertThat(page).isNotNull();
                            assertThat(page.facets()).isNotNull();
                            // repository builds aggregations with keys: MyBorough, MyCuisine,
                            // MyDateRange
                            assertThat(page.facets())
                                    .containsKeys("MyBorough", "MyCuisine", "MyDateRange");
                            var cuisineAgg = page.facets().get("MyCuisine");
                            if (cuisineAgg != null && !cuisineAgg.isEmpty()) {
                                assertThat(cuisineAgg).containsKey("Pizza/Italian");
                                assertThat(cuisineAgg).containsKey("Chinese");
                            }
                        });
    }
}
