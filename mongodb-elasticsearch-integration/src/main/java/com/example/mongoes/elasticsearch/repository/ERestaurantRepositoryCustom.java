package com.example.mongoes.elasticsearch.repository;

import com.example.mongoes.elasticsearch.domain.ERestaurant;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import reactor.core.publisher.Flux;

public interface ERestaurantRepositoryCustom {

  Flux<SearchHit<ERestaurant>> searchWithin(GeoPoint geoPoint, Double distance, String unit);
}
