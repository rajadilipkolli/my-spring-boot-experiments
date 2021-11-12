package com.example.mongoes.mongodb.repository;

import com.example.mongoes.mongodb.domain.Restaurant;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface RestaurantRepository extends ReactiveMongoRepository<Restaurant, String> {
}
