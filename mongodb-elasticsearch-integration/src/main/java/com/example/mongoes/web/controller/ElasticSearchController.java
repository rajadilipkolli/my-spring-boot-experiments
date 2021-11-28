package com.example.mongoes.web.controller;

import com.example.mongoes.web.response.ResultData;
import com.example.mongoes.web.service.ElasticSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class ElasticSearchController {

  private final ElasticSearchService elasticSearchService;

  @GetMapping("/withInRange")
  Flux<ResultData> searchRestaurantsWithInRange(
      @RequestParam Double lat,
      @RequestParam Double lon,
      @RequestParam Double distance,
      @RequestParam(defaultValue = "km", required = false) String unit) {
    return elasticSearchService.searchRestaurantsWithInRange(lat, lon, distance, unit);
  }
}
