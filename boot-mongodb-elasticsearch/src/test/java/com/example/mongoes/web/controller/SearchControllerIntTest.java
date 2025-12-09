package com.example.mongoes.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.mongoes.common.AbstractIntegrationTest;
import com.example.mongoes.document.Address;
import com.example.mongoes.document.Grades;
import com.example.mongoes.document.Restaurant;
import com.example.mongoes.model.response.SearchPageResponse;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.geo.Point;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriBuilder;

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
        restaurant1.setName("Yono gardens");
        restaurant1.setBorough(BOROUGH_NAME);
        restaurant1.setCuisine("Chinese");
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
                                        .queryParam("query", "Chinese")
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
                            // deterministic dataset -> expect exactly one matching document
                            assertThat(page.totalElements()).isEqualTo(1L);
                            assertThat(page.totalHits()).isGreaterThanOrEqualTo(1L);
                            assertThat(page.data()).isNotNull().hasSize(1);
                            var first = page.data().getFirst();
                            assertThat(first.getName()).isEqualTo("Yono gardens");
                            assertThat(first.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(first.getCuisine()).isEqualTo("Chinese");
                            assertThat(first.getGrades()).hasSize(2);
                            assertThat(first.getAddress()).isNotNull();
                            assertThat(first.getAddress().getLocation())
                                    .isEqualTo(new Point(-74.0, 40.7));
                            assertThat(page.pageNumber()).isEqualTo(1);
                            assertThat(page.totalPages()).isEqualTo(1);
                            assertThat(page.isFirst()).isTrue();
                            assertThat(page.isLast()).isTrue();
                            assertThat(page.hasNext()).isFalse();
                            assertThat(page.hasPrevious()).isFalse();
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
                            assertThat(page.totalElements()).isEqualTo(2L);
                            assertThat(page.data()).isNotNull().hasSize(2);
                            var termFirst = page.data().getFirst();
                            assertThat(termFirst.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(termFirst.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(termFirst.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(page.pageNumber()).isEqualTo(1);
                            assertThat(page.totalPages()).isEqualTo(1);
                            assertThat(page.isFirst()).isTrue();
                            assertThat(page.isLast()).isTrue();
                            assertThat(page.hasNext()).isFalse();
                            assertThat(page.hasPrevious()).isFalse();
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
                            assertThat(page.totalElements()).isEqualTo(2L);
                            assertThat(page.data()).isNotNull().hasSize(2);
                            var termsFirst = page.data().getFirst();
                            assertThat(termsFirst.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(termsFirst.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(termsFirst.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(page.pageNumber()).isEqualTo(1);
                            assertThat(page.totalPages()).isEqualTo(1);
                            assertThat(page.isFirst()).isTrue();
                            assertThat(page.isLast()).isTrue();
                            assertThat(page.hasNext()).isFalse();
                            assertThat(page.hasPrevious()).isFalse();
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
                            assertThat(restaurants).isNotNull().hasSize(1);
                            Restaurant restaurant = restaurants.getFirst();
                            assertThat(restaurant).isNotNull();
                            assertThat(restaurant.getId()).isNotBlank();
                            assertThat(restaurant.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant.getAddress()).isNotNull();
                            assertThat(restaurant.getAddress().getBuilding())
                                    .isEqualTo("junitBuilding");
                            assertThat(restaurant.getAddress().getLocation())
                                    .isEqualTo(new Point(-73.9, 40.8));
                            assertThat(restaurant.getAddress().getStreet())
                                    .isEqualTo("junitStreet");
                            assertThat(restaurant.getAddress().getZipcode()).isEqualTo(98765);
                            assertThat(restaurant.getGrades()).isNotNull().hasSize(2);
                            var g0 = restaurant.getGrades().getFirst();
                            assertThat(g0.getGrade()).isEqualTo("A");
                            assertThat(g0.getDate())
                                    .isEqualTo(LocalDateTime.of(2024, 1, 1, 1, 1, 1));
                            assertThat(g0.getScore()).isEqualTo(15);
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
                            assertThat(page.totalElements()).isEqualTo(2L);
                            assertThat(page.totalHits()).isGreaterThanOrEqualTo(2L);
                            assertThat(page.data()).isNotNull().hasSize(2);
                            var shouldFirst = page.data().getFirst();
                            assertThat(shouldFirst.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(shouldFirst.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(shouldFirst.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(page.pageNumber()).isEqualTo(1);
                            assertThat(page.totalPages()).isEqualTo(1);
                            assertThat(page.isFirst()).isTrue();
                            assertThat(page.isLast()).isTrue();
                            assertThat(page.hasNext()).isFalse();
                            assertThat(page.hasPrevious()).isFalse();
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
                            // expect exact match
                            assertThat(page.totalElements()).isEqualTo(2L);
                            assertThat(page.data()).isNotNull().hasSize(2);
                            var wcFirst = page.data().getFirst();
                            assertThat(wcFirst.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(wcFirst.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(wcFirst.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(page.pageNumber()).isEqualTo(1);
                            assertThat(page.totalPages()).isEqualTo(1);
                            assertThat(page.isFirst()).isTrue();
                            assertThat(page.isLast()).isTrue();
                            assertThat(page.hasNext()).isFalse();
                            assertThat(page.hasPrevious()).isFalse();
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
                            assertThat(page.isFirst()).isTrue();
                            assertThat(page.isLast()).isTrue();
                            assertThat(page.hasNext()).isFalse();
                            assertThat(page.hasPrevious()).isFalse();
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
                            assertThat(page.totalElements()).isEqualTo(0L);
                            assertThat(page.totalHits()).isEqualTo(0L);
                            assertThat(page.data()).isNotNull().isEmpty();
                            assertThat(page.pageNumber()).isEqualTo(1);
                            assertThat(page.totalPages()).isEqualTo(0);
                            assertThat(page.isFirst()).isTrue();
                            assertThat(page.isLast()).isTrue();
                            assertThat(page.hasNext()).isFalse();
                            assertThat(page.hasPrevious()).isFalse();
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
                            var rangeFirst = page.data().getFirst();
                            assertThat(rangeFirst.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(rangeFirst.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(rangeFirst.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(page.pageNumber()).isEqualTo(1);
                            assertThat(page.totalPages()).isEqualTo(1);
                            assertThat(page.isFirst()).isTrue();
                            assertThat(page.isLast()).isTrue();
                            assertThat(page.hasNext()).isFalse();
                            assertThat(page.hasPrevious()).isFalse();
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
                            var dateFirst = page.data().getFirst();
                            assertThat(dateFirst.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(dateFirst.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(dateFirst.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(page.pageNumber()).isEqualTo(1);
                            assertThat(page.totalPages()).isEqualTo(1);
                            assertThat(page.isFirst()).isTrue();
                            assertThat(page.isLast()).isTrue();
                            assertThat(page.hasNext()).isFalse();
                            assertThat(page.hasPrevious()).isFalse();
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
                                        .queryParam("searchKeyword", "chinese")
                                        .queryParam("offset", 0)
                                        .queryParam("limit", 10)
                                        .queryParam("fieldNames", "restaurant_name", "cuisine")
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
                            assertThat(cuisineAgg).isNotNull();
                            assertThat(cuisineAgg).containsKey("chinese");
                            assertThat(cuisineAgg.get("chinese")).isGreaterThanOrEqualTo(1L);
                            var boroughAgg = page.facets().get("MyBorough");
                            assertThat(boroughAgg).isNotNull();
                            assertThat(boroughAgg).containsKey("chinese");
                            assertThat(boroughAgg.get("chinese")).isGreaterThanOrEqualTo(1L);
                            assertThat(page.isFirst()).isTrue();
                            assertThat(page.isLast()).isTrue();
                            assertThat(page.hasNext()).isFalse();
                            assertThat(page.hasPrevious()).isFalse();
                        });
    }

    @Test
    @Disabled
    void withInRangeEndPoint() {
        Function<UriBuilder, URI> uriFunction =
                uriBuilder ->
                        uriBuilder
                                .path("/search/restaurant/withInRange")
                                .queryParam("lat", -73.9)
                                .queryParam("lon", 40.8)
                                .queryParam("distance", 50)
                                .queryParam("unit", "km")
                                .build();

        this.webTestClient
                .get()
                .uri(uriFunction)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(String.class)
                .hasSize(1);
    }
}
