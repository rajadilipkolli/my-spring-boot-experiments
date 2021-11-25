package com.example.mongoes.mongodb.repository;

import com.example.mongoes.mongodb.domain.Restaurant;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RestaurantRepository extends MongoRepository<Restaurant, String> {}
