package com.example.mongoes.web.service;

import com.example.mongoes.web.response.ResultData;
import reactor.core.publisher.Flux;

public interface ElasticSearchService {
  Flux<ResultData> searchRestaurantsWithInRange(Double lat, Double lon, Double distance, String unit);
}
