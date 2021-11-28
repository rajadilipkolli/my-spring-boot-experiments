package com.example.mongoes.web.service;

import com.example.mongoes.elasticsearch.domain.ERestaurant;
import com.example.mongoes.mongodb.domain.Notes;
import com.example.mongoes.mongodb.domain.Restaurant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RestaurantService {

  Mono<Restaurant> addNotesToRestaurant(String restaurantName, Notes notes);

  Restaurant createRestaurant(Restaurant restaurant);

  Mono<ERestaurant> findByRestaurantName(String restaurantName);

  Flux<ERestaurant> findAllRestaurants();
}
