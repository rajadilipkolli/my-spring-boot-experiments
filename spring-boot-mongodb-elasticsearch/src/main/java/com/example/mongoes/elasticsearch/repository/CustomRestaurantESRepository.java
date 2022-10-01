package com.example.mongoes.elasticsearch.repository;

import com.example.mongoes.document.Restaurant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.AggregationContainer;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomRestaurantESRepository {

    Flux<SearchHit<Restaurant>> searchWithin(GeoPoint geoPoint, Double distance, String unit);

    Mono<SearchPage<Restaurant>> findByBoroughOrCuisineOrName(
            String query, Boolean prefixPhraseEnabled, Pageable pageable);

    Mono<SearchPage<Restaurant>> queryBoroughKeywordTerm(String query, Pageable pageable);

    Mono<SearchPage<Restaurant>> termQueryForBorough(String query, Pageable pageable);

    Mono<SearchPage<Restaurant>> termsQueryForBorough(List<String> queries, Pageable pageable);

    Mono<SearchPage<Restaurant>> queryBoolWithShould(
            String borough, String cuisine, String name, Pageable pageable);

    Mono<SearchPage<Restaurant>> wildcardSearch(String queryKeyword, Pageable pageable);

    Mono<SearchPage<Restaurant>> regExpSearch(String queryKeyword, Pageable pageable);

    Mono<SearchPage<Restaurant>> searchSimpleQueryForBoroughAndCuisine(
            String queryKeyword, Pageable pageable);

    Mono<SearchPage<Restaurant>> searchRestaurantIdRange(
            Long lowerLimit, Long upperLimit, Pageable pageable);

    Mono<SearchPage<Restaurant>> searchDateRange(String fromDate, String toDate, Pageable pageable);

    Flux<? extends AggregationContainer<?>> aggregateSearch(
            NativeSearchQueryBuilder nativeSearchQueryBuilder);

    Mono<SearchPage<Restaurant>> searchResultsForFacets(
            NativeSearchQueryBuilder nativeSearchQueryBuilder, Integer limit, Integer offset);
}
