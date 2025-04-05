package com.example.mongoes.repository.elasticsearch;

import com.example.mongoes.document.Restaurant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomRestaurantESRepository {

    Flux<SearchHit<Restaurant>> searchWithin(GeoPoint geoPoint, Double distance, String unit);

    Mono<SearchPage<Restaurant>> findByBoroughOrCuisineOrName(
            String query, Boolean prefixPhraseEnabled, Pageable pageable);

    Mono<SearchPage<Restaurant>> termQueryForBorough(String query, Pageable pageable);

    Mono<SearchPage<Restaurant>> termsQueryForBorough(List<String> queries, Pageable pageable);

    Mono<SearchPage<Restaurant>> queryBoolWithShould(
            String borough, String cuisine, String name, Pageable pageable);

    Mono<SearchPage<Restaurant>> wildcardSearch(String queryKeyword, Pageable pageable);

    Mono<SearchPage<Restaurant>> regExpSearch(String reqEx, Pageable pageable);

    Mono<SearchPage<Restaurant>> searchSimpleQueryForBoroughAndCuisine(
            String queryKeyword, Pageable pageable);

    Mono<SearchPage<Restaurant>> searchRestaurantIdRange(
            Long lowerLimit, Long upperLimit, Pageable pageable);

    Mono<SearchPage<Restaurant>> searchDateRange(String fromDate, String toDate, Pageable pageable);

    Mono<SearchPage<Restaurant>> aggregateSearch(
            String searchKeyword,
            List<String> fieldNames,
            Sort.Direction direction,
            Integer limit,
            Integer offset,
            String[] sortFields);

    Mono<SearchPage<Restaurant>> findAll(Pageable pageable);
}
