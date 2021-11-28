package com.example.mongoes.mongodb.repository;

import com.example.mongoes.mongodb.domain.Restaurant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RestaurantRepository extends MongoRepository<Restaurant, String> {
  Optional<Restaurant> findByRestaurantName(String restaurantName);
}
