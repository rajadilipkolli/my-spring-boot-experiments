package com.example.mongoes.elasticsearch.repository;

import com.example.mongoes.elasticsearch.domain.ERestaurant;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.GeoDistanceOrder;
import org.springframework.data.elasticsearch.core.query.Query;
import reactor.core.publisher.Flux;

public class ERestaurantRepositoryCustomImpl implements ERestaurantRepositoryCustom {

  private final ReactiveElasticsearchOperations reactiveElasticsearchOperations;

  public ERestaurantRepositoryCustomImpl(
      ReactiveElasticsearchOperations reactiveElasticsearchOperations) {
    this.reactiveElasticsearchOperations = reactiveElasticsearchOperations;
  }

  @Override
  public Flux<SearchHit<ERestaurant>> searchWithin(
      GeoPoint geoPoint, Double distance, String unit) {

    Query query =
        new CriteriaQuery(new Criteria("location").within(geoPoint, distance.toString() + unit));

    // add a sort to get the actual distance back in the sort value
    Sort sort = Sort.by(new GeoDistanceOrder("location", geoPoint).withUnit(unit));
    query.addSort(sort);

    return reactiveElasticsearchOperations.search(query, ERestaurant.class);
  }
}
