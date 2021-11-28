package com.example.mongoes.web.service.impl;

import com.example.mongoes.elasticsearch.domain.ERestaurant;
import com.example.mongoes.elasticsearch.repository.ERestaurantRepository;
import com.example.mongoes.web.response.ResultData;
import com.example.mongoes.web.service.ElasticSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ElasticSearchServiceImpl implements ElasticSearchService {

  private final ERestaurantRepository eRestaurantRepository;

  @Override
  public Flux<ResultData> searchRestaurantsWithInRange(Double lat, Double lon, Double distance, String unit) {
    GeoPoint location = new GeoPoint(lat, lon);
    return eRestaurantRepository
        .searchWithin(location, distance, unit)
        .flatMap(
            eRestaurantSearchHit -> {
              Double dist = (Double) eRestaurantSearchHit.getSortValues().get(0);
              ERestaurant eRestaurant = eRestaurantSearchHit.getContent();
              return Mono.just(
                  new ResultData(eRestaurant.getRestaurantName(), eRestaurant.getLocation(), dist));
            });
  }
}
