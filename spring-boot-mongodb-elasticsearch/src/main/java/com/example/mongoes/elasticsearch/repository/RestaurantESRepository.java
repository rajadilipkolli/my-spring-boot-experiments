package com.example.mongoes.elasticsearch.repository;

import com.example.mongoes.document.Restaurant;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Mono;

public interface RestaurantESRepository
        extends ReactiveElasticsearchRepository<Restaurant, String> {
    Mono<Restaurant> findByRestaurantId(Long restaurantId);

    Mono<Restaurant> findByName(String restaurantName);
}
