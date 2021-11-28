package com.example.mongoes.elasticsearch.repository;

import com.example.mongoes.elasticsearch.domain.ERestaurant;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;

public interface ERestaurantRepository
    extends ReactiveElasticsearchRepository<ERestaurant, String> {}
