package com.example.opensearch.repositories;

import com.example.opensearch.entities.Restaurant;
import com.example.opensearch.model.response.PagedResult;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.SearchHitsIterator;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

public interface CustomRestaurantRepository {
    SearchHitsIterator<Restaurant> searchWithin(GeoPoint geoPoint, Double distance, String unit);

    PagedResult<Restaurant> findByBoroughOrCuisineOrName(String query, Boolean prefixPhraseEnabled, Pageable pageable);

    PagedResult<Restaurant> termQueryForBorough(String queryTerm, Pageable pageable);

    PagedResult<Restaurant> termsQueryForBorough(List<String> queries, Pageable pageable);

    PagedResult<Restaurant> queryBoolWithShould(String borough, String cuisine, String name, Pageable pageable);

    PagedResult<Restaurant> wildcardSearch(String queryKeyword, Pageable pageable);

    PagedResult<Restaurant> regExpSearch(String reqEx, Pageable pageable);

    PagedResult<Restaurant> searchSimpleQueryForBoroughAndCuisine(String queryKeyword, Pageable pageable);

    PagedResult<Restaurant> searchRestaurantIdRange(Long lowerLimit, Long upperLimit, Pageable pageable);

    PagedResult<Restaurant> searchDateRange(String fromDate, String toDate, Pageable pageable);

    SearchPage<Restaurant> aggregateSearch(
            String searchKeyword,
            List<String> fieldNames,
            Sort.Direction direction,
            Integer limit,
            Integer offset,
            String[] sortFields);
}
