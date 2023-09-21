package com.example.opensearch.repositories;

import com.example.opensearch.entities.Restaurant;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository
        extends ElasticsearchRepository<Restaurant, String>,
                ListCrudRepository<Restaurant, String> {}
