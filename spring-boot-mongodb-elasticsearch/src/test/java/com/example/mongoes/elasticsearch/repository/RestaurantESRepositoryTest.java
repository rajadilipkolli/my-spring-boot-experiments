package com.example.mongoes.elasticsearch.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.mongoes.common.ElasticsearchContainerSetUp;
import com.example.mongoes.config.DataStoreConfiguration;
import com.example.mongoes.document.Restaurant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

@DataElasticsearchTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(DataStoreConfiguration.class)
class RestaurantESRepositoryTest extends ElasticsearchContainerSetUp {

    public static final String RESTAURANT_NAME = "JunitRestaurant";
    private static final String BOROUGH_NAME = "JunitBorough";
    private static final String CUISINE_NAME = "JunitCuisine";
    @Autowired private RestaurantESRepository restaurantESRepository;

    @BeforeAll
    void setUpData() {
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(1L);
        restaurant.setName(RESTAURANT_NAME);
        restaurant.setBorough(BOROUGH_NAME);
        restaurant.setCuisine(CUISINE_NAME);
        this.restaurantESRepository.save(restaurant);
    }

    @Test
    void testFindByRestaurantId() {
        this.restaurantESRepository
                .findByRestaurantId(1L)
                .subscribe(
                        restaurant1 -> {
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(1L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                        });
    }

    @Test
    void testFindByName() {
        this.restaurantESRepository
                .findByName(RESTAURANT_NAME)
                .subscribe(
                        restaurant1 -> {
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(1L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                        });
    }

    @Test
    void testFindByBorough() {
        this.restaurantESRepository
                .findByBorough(BOROUGH_NAME, PageRequest.of(0, 10))
                .subscribe(
                        restaurant1 -> {
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(1L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                        });
    }

    @Test
    void testFindByBoroughAndCuisineAndName() {
        this.restaurantESRepository
                .findByBoroughAndCuisineAndName(
                        BOROUGH_NAME, CUISINE_NAME, RESTAURANT_NAME, PageRequest.of(0, 10))
                .subscribe(
                        restaurant1 -> {
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(1L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                        });
    }

    @Test
    void testFindByBoroughOrCuisineOrName() {
        this.restaurantESRepository
                .findByBoroughOrCuisineOrName(BOROUGH_NAME, false, PageRequest.of(0, 10))
                .subscribe(
                        searchPage -> {
                            assertThat(searchPage.getNumberOfElements()).isEqualTo(1);
                            assertThat(searchPage.getTotalPages()).isEqualTo(1);
                            assertThat(searchPage.isFirst()).isEqualTo(true);
                            assertThat(searchPage.isLast()).isEqualTo(true);
                            assertThat(searchPage.isEmpty()).isEqualTo(false);
                            assertThat(searchPage.hasContent()).isEqualTo(true);
                            assertThat(searchPage.stream().count()).isEqualTo(1);
                            Restaurant restaurant1 = searchPage.getContent().get(0).getContent();
                            assertThat(restaurant1.getRestaurantId()).isEqualTo(1L);
                            assertThat(restaurant1.getName()).isEqualTo(RESTAURANT_NAME);
                            assertThat(restaurant1.getBorough()).isEqualTo(BOROUGH_NAME);
                            assertThat(restaurant1.getCuisine()).isEqualTo(CUISINE_NAME);
                        });
    }
}
