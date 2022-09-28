package com.example.mongoes.mongodb.repository;

import com.example.mongoes.mongodb.document.Restaurant;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface RestaurantRepository extends ReactiveMongoRepository<Restaurant, String> {

    Mono<Restaurant> findByName(String restaurantName);

    Mono<Restaurant> findByRestaurantId(Long restaurantId);
}
