package com.example.mongoes.web.controller;

import com.example.mongoes.elasticsearch.domain.ERestaurant;
import com.example.mongoes.mongodb.domain.Notes;
import com.example.mongoes.mongodb.domain.Restaurant;
import com.example.mongoes.web.api.RestaurantApi;
import com.example.mongoes.web.response.GenericMessage;
import com.example.mongoes.web.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class RestaurantController implements RestaurantApi {

  private final RestaurantService restaurantService;

  @GetMapping("/restaurant/")
  public ResponseEntity<Flux<ERestaurant>> findAllRestaurants() {
    return ResponseEntity.ok(restaurantService.findAllRestaurants());
  }

  @GetMapping("/restaurant/{restaurantName}")
  @Override
  public Mono<ResponseEntity<ERestaurant>> findRestaurantByName(
      @PathVariable("restaurantName") String restaurantName) {
    return restaurantService
        .findByRestaurantName(restaurantName)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PostMapping("/restaurant")
  @Override
  public ResponseEntity<Object> createRestaurant(@RequestBody Restaurant restaurant) {
    return ResponseEntity.created(
            URI.create(
                String.format(
                    "/restaurant/%s",
                    this.restaurantService.createRestaurant(restaurant).getRestaurantName())))
        .body(
            new GenericMessage(
                String.format("restaurant with name %s created", restaurant.getRestaurantName())));
  }

  @PutMapping("/notes/{restaurantName}")
  @Override
  public Mono<ResponseEntity<Restaurant>> addNotesToRestaurant(
      @PathVariable("restaurantName") String restaurantName, @RequestBody Notes notes) {
    return restaurantService.addNotesToRestaurant(restaurantName, notes).map(ResponseEntity::ok);
  }
}
