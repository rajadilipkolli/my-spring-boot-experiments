package com.example.mongoes.elasticsearch.repository;

import com.example.mongoes.document.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.GeoDistanceOrder;
import org.springframework.data.elasticsearch.core.query.Query;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class CustomRestaurantESRepositoryImpl implements CustomRestaurantESRepository {

    private final ReactiveElasticsearchOperations reactiveElasticsearchOperations;

    @Override
    public Flux<SearchHit<Restaurant>> searchWithin(
            GeoPoint geoPoint, Double distance, String unit) {

        Query query =
                new CriteriaQuery(
                        new Criteria("address.coord").within(geoPoint, distance.toString() + unit));

        // add a sort to get the actual distance back in the sort value
        Sort sort = Sort.by(new GeoDistanceOrder("address.coord", geoPoint).withUnit(unit));
        query.addSort(sort);

        return reactiveElasticsearchOperations.search(query, Restaurant.class);
    }
}
