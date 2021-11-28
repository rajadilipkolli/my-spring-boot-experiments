package com.example.mongoes.elasticsearch.repository;

import com.example.mongoes.elasticsearch.domain.ERestaurant;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Mono;

public interface ERestaurantRepository
    extends ReactiveElasticsearchRepository<ERestaurant, String>, ERestaurantRepositoryCustom {

  Mono<ERestaurant> findByRestaurantName(String restaurantName);
}
