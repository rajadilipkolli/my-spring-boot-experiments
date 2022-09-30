package com.example.mongoes.elasticsearch.repository;

import com.example.mongoes.document.Restaurant;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import reactor.core.publisher.Flux;

public interface CustomRestaurantESRepository {

    Flux<SearchHit<Restaurant>> searchWithin(GeoPoint geoPoint, Double distance, String unit);
}
