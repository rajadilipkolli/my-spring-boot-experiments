package com.example.mongoes.repository.elasticsearch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.mongoes.common.AbstractIntegrationTest;
import com.example.mongoes.document.Address;
import com.example.mongoes.document.Grades;
import com.example.mongoes.document.Restaurant;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.geo.Point;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RestaurantESRepositoryIntegrationTest extends AbstractIntegrationTest {

    private static final String RESTAURANT_NAME = "Lb Spumoni Gardens";
    private static final String BOROUGH_NAME = "Brooklyn";
    private static final String CUISINE_NAME = "Pizza/Italian";

    @Autowired private RestaurantESRepository restaurantESRepository;

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
        Address address = new Address();
        address.setLocation(new Point(-73.9, 40.8));
        restaurant.setAddress(address);
        Grades grade = new Grades("A", LocalDateTime.of(2022, 1, 1, 1, 1, 1), 15);
        Grades grade1 = new Grades("B", LocalDateTime.of(2022, 3, 31, 23, 59, 59), 15);
        restaurant.setGrades(List.of(grade, grade1));
        return restaurant;
    }

    @Test
    void findByRestaurantId() {
        var findByRestaurantId = this.restaurantESRepository.findByRestaurantId(1L);

        StepVerifier.create(findByRestaurantId).expectNextCount(0).verifyComplete();

        var findByRestaurantIdMono = this.restaurantESRepository.findByRestaurantId(2L);

        StepVerifier.create(findByRestaurantIdMono)
                .consumeNextWith(this::assertRestaurant)
                .verifyComplete();
    }

    @Test
    void findByName() {
        var findNameMono = this.restaurantESRepository.findByName(RESTAURANT_NAME);

        StepVerifier.create(findNameMono).consumeNextWith(this::assertRestaurant).verifyComplete();
    }

    @Test
    void findByBorough() {
        var findByBoroughMono =
                this.restaurantESRepository.findByBorough(
                        BOROUGH_NAME,
                        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "restaurant_id")));

        StepVerifier.create(findByBoroughMono)
                .consumeNextWith(this::assertRestaurant)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findByBoroughAndCuisineAndName() {
        var findByBoroughAndCuisineAndNameMono =
                this.restaurantESRepository.findByBoroughAndCuisineAndName(
                        BOROUGH_NAME, CUISINE_NAME, RESTAURANT_NAME, PageRequest.of(0, 10));

        StepVerifier.create(findByBoroughAndCuisineAndNameMono)
                .consumeNextWith(this::assertRestaurant)
                .verifyComplete();
    }

    @Test
    void findByBoroughOrCuisineOrName() {
        var findByBoroughOrCuisineOrNameMono =
                this.restaurantESRepository.findByBoroughOrCuisineOrName(
                        BOROUGH_NAME,
                        false,
                        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "restaurant_id")));

        StepVerifier.create(findByBoroughOrCuisineOrNameMono)
                .consumeNextWith(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(2);
                            assertThat(searchPage.getTotalPages()).isEqualTo(1);
                            assertThat(searchPage.isFirst()).isTrue();
                            assertThat(searchPage.isLast()).isTrue();
                            assertThat(searchPage.isEmpty()).isFalse();
                            assertThat(searchPage.hasContent()).isTrue();
                            assertThat(searchPage.stream().count()).isEqualTo(2);
                            Restaurant restaurant1 =
                                    searchPage.getContent().getFirst().getContent();
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    @Disabled
    void searchWithin() {
        Flux<SearchHit<Restaurant>> searchHitFlux =
                this.restaurantESRepository.searchWithin(new GeoPoint(40.8, -73.9), 50d, "km");

        StepVerifier.create(searchHitFlux)
                .consumeNextWith(
                        restaurantSearchHit -> {
                            assertThat(restaurantSearchHit.getId()).isNotEmpty();
                            assertThat(restaurantSearchHit.getIndex()).isEqualTo("restaurant");
                            Restaurant restaurant = restaurantSearchHit.getContent();
                            assertThat(restaurant.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void termQueryForBorough() {
        var termQueryForBoroughMono =
                this.restaurantESRepository.termQueryForBorough(
                        BOROUGH_NAME,
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "restaurant_id")));

        StepVerifier.create(termQueryForBoroughMono)
                .consumeNextWith(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(2);
                            assertThat(searchPage.getTotalPages()).isEqualTo(1);
                            assertThat(searchPage.isFirst()).isTrue();
                            assertThat(searchPage.isLast()).isTrue();
                            assertThat(searchPage.isEmpty()).isFalse();
                            assertThat(searchPage.hasContent()).isTrue();
                            assertThat(searchPage.stream().count()).isEqualTo(2);
                            Restaurant restaurant1 =
                                    searchPage.getContent().getFirst().getContent();
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void termsQueryForBorough() {
        Mono<SearchPage<Restaurant>> response =
                this.restaurantESRepository.termsQueryForBorough(
                        List.of(BOROUGH_NAME),
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "restaurant_id")));

        StepVerifier.create(response)
                .consumeNextWith(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(2);
                            assertThat(searchPage.getTotalPages()).isEqualTo(1);
                            assertThat(searchPage.isFirst()).isTrue();
                            assertThat(searchPage.isLast()).isTrue();
                            assertThat(searchPage.isEmpty()).isFalse();
                            assertThat(searchPage.hasContent()).isTrue();
                            assertThat(searchPage.stream().count()).isEqualTo(2);
                            Restaurant restaurant1 =
                                    searchPage.getContent().getFirst().getContent();
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void queryBoolWithShould() {
        Mono<SearchPage<Restaurant>> queryBoolWithShouldMono =
                this.restaurantESRepository.queryBoolWithShould(
                        BOROUGH_NAME,
                        CUISINE_NAME,
                        RESTAURANT_NAME,
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "restaurant_id")));

        StepVerifier.create(queryBoolWithShouldMono)
                .consumeNextWith(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(2);
                            assertThat(searchPage.getTotalPages()).isEqualTo(1);
                            assertThat(searchPage.isFirst()).isTrue();
                            assertThat(searchPage.isLast()).isTrue();
                            assertThat(searchPage.isEmpty()).isFalse();
                            assertThat(searchPage.hasContent()).isTrue();
                            assertThat(searchPage.stream().count()).isEqualTo(2);
                            Restaurant restaurant1 =
                                    searchPage.getContent().getFirst().getContent();
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void wildcardSearch() {
        var wildcardSearch =
                this.restaurantESRepository.wildcardSearch("ines", PageRequest.of(0, 5));

        StepVerifier.create(wildcardSearch)
                .consumeNextWith(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(1);
                            assertThat(searchPage.getTotalPages()).isEqualTo(1);
                            assertThat(searchPage.isFirst()).isTrue();
                            assertThat(searchPage.isLast()).isTrue();
                            assertThat(searchPage.isEmpty()).isFalse();
                            assertThat(searchPage.hasContent()).isTrue();
                            assertThat(searchPage.stream().count()).isEqualTo(1);
                            Restaurant restaurant1 =
                                    searchPage.getContent().getFirst().getContent();
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(40363920L);
                            assertThat(restaurant1.getName()).isEqualTo("Yono gardens");
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo("Chinese");
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void regExpSearch() {
        var regExpSearchMono =
                this.restaurantESRepository.regExpSearch(
                        "B.[a-z]*",
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "restaurant_id")));

        StepVerifier.create(regExpSearchMono)
                .consumeNextWith(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(2);
                            assertThat(searchPage.getTotalPages()).isEqualTo(1);
                            assertThat(searchPage.isFirst()).isTrue();
                            assertThat(searchPage.isLast()).isTrue();
                            assertThat(searchPage.isEmpty()).isFalse();
                            assertThat(searchPage.hasContent()).isTrue();
                            assertThat(searchPage.stream().count()).isEqualTo(2);
                            Restaurant restaurant1 =
                                    searchPage.getContent().getFirst().getContent();
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void searchSimpleQueryForBoroughAndCuisine() {
        var searchSimpleQueryForBoroughAndCuisine =
                this.restaurantESRepository.searchSimpleQueryForBoroughAndCuisine(
                        BOROUGH_NAME,
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "restaurant_id")));

        StepVerifier.create(searchSimpleQueryForBoroughAndCuisine)
                .consumeNextWith(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(2);
                            assertThat(searchPage.getTotalPages()).isEqualTo(1);
                            assertThat(searchPage.isFirst()).isTrue();
                            assertThat(searchPage.isLast()).isTrue();
                            assertThat(searchPage.isEmpty()).isFalse();
                            assertThat(searchPage.hasContent()).isTrue();
                            assertThat(searchPage.stream().count()).isEqualTo(2);
                            Restaurant restaurant1 =
                                    searchPage.getContent().getFirst().getContent();
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void searchRestaurantIdRange() {
        Mono<SearchPage<Restaurant>> searchRestaurantIdRange =
                this.restaurantESRepository.searchRestaurantIdRange(0L, 10L, PageRequest.of(0, 5));

        StepVerifier.create(searchRestaurantIdRange)
                .consumeNextWith(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(1);
                            assertThat(searchPage.getTotalPages()).isEqualTo(1);
                            assertThat(searchPage.isFirst()).isTrue();
                            assertThat(searchPage.isLast()).isTrue();
                            assertThat(searchPage.isEmpty()).isFalse();
                            assertThat(searchPage.hasContent()).isTrue();
                            assertThat(searchPage.stream().count()).isEqualTo(1);
                            Restaurant restaurant1 =
                                    searchPage.getContent().getFirst().getContent();
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void searchDateRange() {
        var searchDateRangeMono =
                this.restaurantESRepository.searchDateRange(
                        LocalDateTime.of(2021, 12, 31, 23, 59, 59).toString(),
                        LocalDateTime.of(2022, 4, 11, 0, 0, 0).toString(),
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "restaurant_id")));

        StepVerifier.create(searchDateRangeMono)
                .consumeNextWith(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(2);
                            assertThat(searchPage.getTotalPages()).isEqualTo(1);
                            assertThat(searchPage.isFirst()).isTrue();
                            assertThat(searchPage.isLast()).isTrue();
                            assertThat(searchPage.isEmpty()).isFalse();
                            assertThat(searchPage.hasContent()).isTrue();
                            assertThat(searchPage.stream().count()).isEqualTo(2);
                            Restaurant restaurant1 =
                                    searchPage.getContent().getFirst().getContent();
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void findAll() {
        Mono<SearchPage<Restaurant>> findAllMono =
                this.restaurantESRepository.findAll(
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "restaurant_id")));

        StepVerifier.create(findAllMono)
                .consumeNextWith(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(2);
                            assertThat(searchPage.getTotalPages()).isEqualTo(1);
                            assertThat(searchPage.isFirst()).isTrue();
                            assertThat(searchPage.isLast()).isTrue();
                            assertThat(searchPage.isEmpty()).isFalse();
                            assertThat(searchPage.hasContent()).isTrue();
                            assertThat(searchPage.stream().count()).isEqualTo(2);
                            Restaurant restaurant1 =
                                    searchPage.getContent().getFirst().getContent();
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void aggregation() {
        Mono<SearchPage<Restaurant>> aggregationMono =
                this.restaurantESRepository.aggregateSearch(
                        "Pizza",
                        List.of("restaurant_name", "borough", "cuisine"),
                        Sort.Direction.ASC,
                        10,
                        0,
                        new String[] {"restaurant_id"});

        StepVerifier.create(aggregationMono)
                .consumeNextWith(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(1);
                            assertThat(searchPage.getTotalPages()).isEqualTo(1);
                            assertThat(searchPage.isFirst()).isTrue();
                            assertThat(searchPage.isLast()).isTrue();
                            assertThat(searchPage.isEmpty()).isFalse();
                            assertThat(searchPage.hasContent()).isTrue();
                            assertThat(searchPage.getSearchHits().getAggregations())
                                    .isNotNull()
                                    .isExactlyInstanceOf(ElasticsearchAggregations.class);
                            assertThat(searchPage.stream().count()).isEqualTo(1);
                            Restaurant restaurant1 =
                                    searchPage.getContent().getFirst().getContent();
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                            ElasticsearchAggregations elasticsearchAggregations =
                                    (ElasticsearchAggregations)
                                            searchPage.getSearchHits().getAggregations();
                            assertThat(elasticsearchAggregations.aggregations()).isNotNull();
                            Map<String, ElasticsearchAggregation> aggregationMap =
                                    elasticsearchAggregations.aggregationsAsMap();
                            assertThat(aggregationMap).isNotEmpty().hasSize(3);
                            assertThat(aggregationMap)
                                    .containsOnlyKeys("MyCuisine", "MyBorough", "MyDateRange");
                        })
                .verifyComplete();
    }

    private void assertRestaurant(Restaurant restaurant) {
        assertThat(restaurant.getRestaurantId()).isEqualTo(2);
        assertThat(restaurant.getName()).isEqualTo(RESTAURANT_NAME);
        assertThat(restaurant.getBorough()).isEqualTo(BOROUGH_NAME);
        assertThat(restaurant.getCuisine()).isEqualTo(CUISINE_NAME);
        assertThat(restaurant.getGrades()).isNotEmpty().hasSize(2);
        assertThat(restaurant.getAddress()).isNotNull();
        assertThat(restaurant.getAddress().getLocation())
                .isNotNull()
                .extracting(Point::getX, Point::getY)
                .containsExactly(-73.9, 40.8);
    }
}
