package com.example.opensearch.repositories;

import com.example.opensearch.entities.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository
        extends ElasticsearchRepository<Restaurant, String>,
                ListCrudRepository<Restaurant, String>,
                CustomRestaurantRepository {

    Page<Restaurant> findByBorough(String borough, Pageable pageable);

    Page<Restaurant> findByBoroughAndCuisineAndName(String borough, String cuisine, String name, Pageable pageable);
}
