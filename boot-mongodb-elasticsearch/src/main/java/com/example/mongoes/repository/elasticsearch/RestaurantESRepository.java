package com.example.mongoes.repository.elasticsearch;

import com.example.mongoes.document.Restaurant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RestaurantESRepository
        extends ReactiveElasticsearchRepository<Restaurant, String>, CustomRestaurantESRepository {
    Mono<Restaurant> findByRestaurantId(Long restaurantId);

    Mono<Restaurant> findByName(String restaurantName);

    Flux<Restaurant> findByBorough(String borough, Pageable pageable);

    Flux<Restaurant> findByBoroughAndCuisineAndName(
            String borough, String cuisine, String name, Pageable pageable);
}
