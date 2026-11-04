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

    /**
     * Searches by similarity to the cuisine vector.
     *
     * @param vector query vector for the three-dimensional cuisine field
     * @param k number of nearest neighbors to retrieve
     * @param pageable requested result page
     * @return a publisher of the search page; Elasticsearch errors propagate to subscribers
     */
    Mono<SearchPage<Restaurant>> knnSearch(List<Float> vector, int k, Pageable pageable);

    /**
     * Combines cuisine vector similarity with a text search across borough, cuisine, and name.
     *
     * @param queryKeyword text to match
     * @param vector query vector for the three-dimensional cuisine field
     * @param k number of nearest neighbors to retrieve
     * @param pageable requested result page
     * @return a publisher of the search page; Elasticsearch errors propagate to subscribers
     */
    Mono<SearchPage<Restaurant>> hybridSearch(
            String queryKeyword, List<Float> vector, int k, Pageable pageable);

    /**
     * Returns the average of indexed grade scores, or 0.0 when the average aggregation or its value
     * is absent. Elasticsearch errors propagate to subscribers.
     */
    Mono<Double> averageInspectionScore();
}
