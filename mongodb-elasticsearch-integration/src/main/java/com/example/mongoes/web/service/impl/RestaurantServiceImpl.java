package com.example.mongoes.web.service.impl;

import com.example.mongoes.elasticsearch.domain.ERestaurant;
import com.example.mongoes.elasticsearch.repository.ERestaurantRepository;
import com.example.mongoes.mongodb.domain.Notes;
import com.example.mongoes.mongodb.domain.Restaurant;
import com.example.mongoes.mongodb.repository.RestaurantRepository;
import com.example.mongoes.web.exception.RestaurantNotFoundException;
import com.example.mongoes.web.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

  private final RestaurantRepository restaurantRepository;
  private final ERestaurantRepository eRestaurantRepository;

  @Override
  public Mono<Restaurant> addNotesToRestaurant(String restaurantName, Notes notes) {
    Restaurant persistedRestaurant =
        this.restaurantRepository
            .findByRestaurantName(restaurantName)
            .orElseThrow(() -> new RestaurantNotFoundException(restaurantName));
    persistedRestaurant.getNotes().add(notes);
    return Mono.just(this.restaurantRepository.save(persistedRestaurant));
  }

  @Override
  public Restaurant createRestaurant(Restaurant restaurant) {
    return this.restaurantRepository.save(restaurant);
  }

  @Override
  public Mono<ERestaurant> findByRestaurantName(String restaurantName) {
    return eRestaurantRepository.findByRestaurantName(restaurantName);
  }

  @Override
  public Flux<ERestaurant> findAllRestaurants() {
    return this.eRestaurantRepository.findAll();
  }
}
