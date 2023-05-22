package com.example.mongoes.elasticsearch.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.mongoes.common.ElasticsearchContainerSetUp;
import com.example.mongoes.config.DataStoreConfiguration;
import com.example.mongoes.document.Address;
import com.example.mongoes.document.Grades;
import com.example.mongoes.document.Restaurant;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.context.annotation.Import;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@DataElasticsearchTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(DataStoreConfiguration.class)
class RestaurantESRepositoryTest extends ElasticsearchContainerSetUp {

    public static final String RESTAURANT_NAME = "Lb Spumoni Gardens";
    private static final String BOROUGH_NAME = "Brooklyn";
    private static final String CUISINE_NAME = "Pizza/Italian";
    @Autowired private RestaurantESRepository restaurantESRepository;

    @BeforeAll
    void setUpData() throws InterruptedException {
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(2L);
        restaurant.setName(RESTAURANT_NAME);
        restaurant.setBorough(BOROUGH_NAME);
        restaurant.setCuisine(CUISINE_NAME);
        Address address = new Address();
        address.setLocation(new Point(-73.9, 40.8));
        restaurant.setAddress(address);
        Grades grade = new Grades("A", LocalDateTime.of(2022, 1, 1, 1, 1, 1), 15);
        Grades grade1 = new Grades("B", LocalDateTime.of(2022, 3, 31, 23, 59, 59), 15);
        restaurant.setGrades(List.of(grade, grade1));
        Restaurant restaurant1 = new Restaurant();
        restaurant1.setRestaurantId(40363920L);
        restaurant1.setBorough("Brooklyn");
        restaurant1.setCuisine("Chinese");
        restaurant1.setName("Yono gardens");
        restaurant1.setGrades(List.of(grade, grade1));
        this.restaurantESRepository
                .deleteAll()
                .log()
                .thenMany(this.restaurantESRepository.saveAll(List.of(restaurant, restaurant1)))
                .log("saving restaurant")
                .subscribe();

        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    void testFindByRestaurantId() {
        var findByRestaurantId = this.restaurantESRepository.findByRestaurantId(1L);

        StepVerifier.create(findByRestaurantId).expectNextCount(0).verifyComplete();

        var findByRestaurantIdMono = this.restaurantESRepository.findByRestaurantId(2L);

        StepVerifier.create(findByRestaurantIdMono)
                .consumeNextWith(
                        restaurant1 -> {
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void testFindByName() {
        var findNameMono = this.restaurantESRepository.findByName(RESTAURANT_NAME);

        StepVerifier.create(findNameMono)
                .consumeNextWith(
                        restaurant1 -> {
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void testFindByBorough() {
        var findByBoroughMono =
                this.restaurantESRepository.findByBorough(
                        BOROUGH_NAME,
                        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "restaurant_id")));

        StepVerifier.create(findByBoroughMono)
                .consumeNextWith(
                        restaurant1 -> {
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void testFindByBoroughAndCuisineAndName() {
        var findByBoroughAndCuisineAndNameMono =
                this.restaurantESRepository.findByBoroughAndCuisineAndName(
                        BOROUGH_NAME, CUISINE_NAME, RESTAURANT_NAME, PageRequest.of(0, 10));

        StepVerifier.create(findByBoroughAndCuisineAndNameMono)
                .consumeNextWith(
                        restaurant1 -> {
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void testFindByBoroughOrCuisineOrName() {
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
                            assertThat(searchPage.isFirst()).isEqualTo(true);
                            assertThat(searchPage.isLast()).isEqualTo(true);
                            assertThat(searchPage.isEmpty()).isEqualTo(false);
                            assertThat(searchPage.hasContent()).isEqualTo(true);
                            assertThat(searchPage.stream().count()).isEqualTo(2);
                            Restaurant restaurant1 = searchPage.getContent().get(0).getContent();
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
    void testSearchWithin() {
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
    void testTermQueryForBorough() {
        var termQueryForBoroughMono =
                this.restaurantESRepository.termQueryForBorough(
                        BOROUGH_NAME,
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "restaurant_id")));

        StepVerifier.create(termQueryForBoroughMono)
                .consumeNextWith(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(2);
                            assertThat(searchPage.getTotalPages()).isEqualTo(1);
                            assertThat(searchPage.isFirst()).isEqualTo(true);
                            assertThat(searchPage.isLast()).isEqualTo(true);
                            assertThat(searchPage.isEmpty()).isEqualTo(false);
                            assertThat(searchPage.hasContent()).isEqualTo(true);
                            assertThat(searchPage.stream().count()).isEqualTo(2);
                            Restaurant restaurant1 = searchPage.getContent().get(0).getContent();
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void testTermsQueryForBorough() {
        Mono<SearchPage<Restaurant>> response =
                this.restaurantESRepository.termsQueryForBorough(
                        List.of(BOROUGH_NAME),
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "restaurant_id")));

        StepVerifier.create(response)
                .consumeNextWith(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(2);
                            assertThat(searchPage.getTotalPages()).isEqualTo(1);
                            assertThat(searchPage.isFirst()).isEqualTo(true);
                            assertThat(searchPage.isLast()).isEqualTo(true);
                            assertThat(searchPage.isEmpty()).isEqualTo(false);
                            assertThat(searchPage.hasContent()).isEqualTo(true);
                            assertThat(searchPage.stream().count()).isEqualTo(2);
                            Restaurant restaurant1 = searchPage.getContent().get(0).getContent();
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void testQueryBoolWithShould() {
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
                            assertThat(searchPage.isFirst()).isEqualTo(true);
                            assertThat(searchPage.isLast()).isEqualTo(true);
                            assertThat(searchPage.isEmpty()).isEqualTo(false);
                            assertThat(searchPage.hasContent()).isEqualTo(true);
                            assertThat(searchPage.stream().count()).isEqualTo(2);
                            Restaurant restaurant1 = searchPage.getContent().get(0).getContent();
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void testWildcardSearch() {
        var wildcardSearchMono =
                this.restaurantESRepository.wildcardSearch("Spumoni", PageRequest.of(0, 5));

        StepVerifier.create(wildcardSearchMono)
                .consumeNextWith(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(0);
                            assertThat(searchPage.getTotalPages()).isEqualTo(0);
                            assertThat(searchPage.isFirst()).isEqualTo(true);
                            assertThat(searchPage.isLast()).isEqualTo(true);
                            assertThat(searchPage.isEmpty()).isEqualTo(true);
                            assertThat(searchPage.hasContent()).isEqualTo(false);
                        })
                .verifyComplete();

        var wildcardSearch =
                this.restaurantESRepository.wildcardSearch("ines", PageRequest.of(0, 5));

        StepVerifier.create(wildcardSearch)
                .consumeNextWith(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(1);
                            assertThat(searchPage.getTotalPages()).isEqualTo(1);
                            assertThat(searchPage.isFirst()).isEqualTo(true);
                            assertThat(searchPage.isLast()).isEqualTo(true);
                            assertThat(searchPage.isEmpty()).isEqualTo(false);
                            assertThat(searchPage.hasContent()).isEqualTo(true);
                            assertThat(searchPage.stream().count()).isEqualTo(1);
                            Restaurant restaurant1 = searchPage.getContent().get(0).getContent();
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(40363920L);
                            assertThat(restaurant1.getName()).isEqualTo("Yono gardens");
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo("Chinese");
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void testRegExpSearch() {
        var regExpSearchMono =
                this.restaurantESRepository.regExpSearch(
                        "B.[a-z]*",
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "restaurant_id")));

        StepVerifier.create(regExpSearchMono)
                .consumeNextWith(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(2);
                            assertThat(searchPage.getTotalPages()).isEqualTo(1);
                            assertThat(searchPage.isFirst()).isEqualTo(true);
                            assertThat(searchPage.isLast()).isEqualTo(true);
                            assertThat(searchPage.isEmpty()).isEqualTo(false);
                            assertThat(searchPage.hasContent()).isEqualTo(true);
                            assertThat(searchPage.stream().count()).isEqualTo(2);
                            Restaurant restaurant1 = searchPage.getContent().get(0).getContent();
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void testSearchSimpleQueryForBoroughAndCuisine() {
        var searchSimpleQueryForBoroughAndCuisine =
                this.restaurantESRepository.searchSimpleQueryForBoroughAndCuisine(
                        BOROUGH_NAME,
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "restaurant_id")));

        StepVerifier.create(searchSimpleQueryForBoroughAndCuisine)
                .consumeNextWith(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(2);
                            assertThat(searchPage.getTotalPages()).isEqualTo(1);
                            assertThat(searchPage.isFirst()).isEqualTo(true);
                            assertThat(searchPage.isLast()).isEqualTo(true);
                            assertThat(searchPage.isEmpty()).isEqualTo(false);
                            assertThat(searchPage.hasContent()).isEqualTo(true);
                            assertThat(searchPage.stream().count()).isEqualTo(2);
                            Restaurant restaurant1 = searchPage.getContent().get(0).getContent();
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void testSearchRestaurantIdRange() {
        Mono<SearchPage<Restaurant>> searchRestaurantIdRange =
                this.restaurantESRepository.searchRestaurantIdRange(0L, 10L, PageRequest.of(0, 5));

        StepVerifier.create(searchRestaurantIdRange)
                .consumeNextWith(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(1);
                            assertThat(searchPage.getTotalPages()).isEqualTo(1);
                            assertThat(searchPage.isFirst()).isEqualTo(true);
                            assertThat(searchPage.isLast()).isEqualTo(true);
                            assertThat(searchPage.isEmpty()).isEqualTo(false);
                            assertThat(searchPage.hasContent()).isEqualTo(true);
                            assertThat(searchPage.stream().count()).isEqualTo(1);
                            Restaurant restaurant1 = searchPage.getContent().get(0).getContent();
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void testSearchDateRange() {
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
                            assertThat(searchPage.isFirst()).isEqualTo(true);
                            assertThat(searchPage.isLast()).isEqualTo(true);
                            assertThat(searchPage.isEmpty()).isEqualTo(false);
                            assertThat(searchPage.hasContent()).isEqualTo(true);
                            assertThat(searchPage.stream().count()).isEqualTo(2);
                            Restaurant restaurant1 = searchPage.getContent().get(0).getContent();
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(2L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                            assertThat(restaurant1.getGrades()).isNotEmpty().hasSize(2);
                        })
                .verifyComplete();
    }

    @Test
    void testFindAll() {
        Mono<SearchPage<Restaurant>> findAllMono =
                this.restaurantESRepository.findAll(
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "restaurant_id")));

        StepVerifier.create(findAllMono)
                .consumeNextWith(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(2);
                            assertThat(searchPage.getTotalPages()).isEqualTo(1);
                            assertThat(searchPage.isFirst()).isEqualTo(true);
                            assertThat(searchPage.isLast()).isEqualTo(true);
                            assertThat(searchPage.isEmpty()).isEqualTo(false);
                            assertThat(searchPage.hasContent()).isEqualTo(true);
                            assertThat(searchPage.stream().count()).isEqualTo(2);
                            Restaurant restaurant1 = searchPage.getContent().get(0).getContent();
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
    void testAggregation() {
        Mono<SearchPage<Restaurant>> aggregationMono =
                this.restaurantESRepository.aggregateSearch(
                        "Pizza",
                        List.of("restautant_name", "borough", "cuisine"),
                        Sort.Direction.ASC,
                        10,
                        0,
                        new String[] {"restaurant_id"});

        StepVerifier.create(aggregationMono)
                .consumeNextWith(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(1);
                            assertThat(searchPage.getTotalPages()).isEqualTo(1);
                            assertThat(searchPage.isFirst()).isEqualTo(true);
                            assertThat(searchPage.isLast()).isEqualTo(true);
                            assertThat(searchPage.isEmpty()).isEqualTo(false);
                            assertThat(searchPage.hasContent()).isEqualTo(true);
                            assertThat(searchPage.getSearchHits().getAggregations())
                                    .isNotNull()
                                    .isExactlyInstanceOf(ElasticsearchAggregation.class);
                            assertThat(searchPage.stream().count()).isEqualTo(1);
                            Restaurant restaurant1 = searchPage.getContent().get(0).getContent();
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
                                    .containsOnlyKeys("MyCuisine", "MyDateRange", "MyBorough");
                        })
                .verifyComplete();
    }
}
