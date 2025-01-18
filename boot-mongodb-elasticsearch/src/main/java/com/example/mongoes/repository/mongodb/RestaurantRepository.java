package com.example.mongoes.repository.mongodb;

import com.example.mongoes.document.Restaurant;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface RestaurantRepository extends ReactiveMongoRepository<Restaurant, String> {

    Mono<Restaurant> findByName(String restaurantName);

    Mono<Restaurant> findByRestaurantId(Long restaurantId);
}
