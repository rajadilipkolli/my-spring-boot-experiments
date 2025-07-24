package com.example.opensearch.services;

import com.example.opensearch.entities.Restaurant;
import com.example.opensearch.model.response.PagedResult;
import com.example.opensearch.model.response.ResultData;
import com.example.opensearch.repositories.RestaurantRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.opensearch.data.client.orhlc.OpenSearchAggregations;
import org.opensearch.search.aggregations.Aggregation;
import org.opensearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.opensearch.search.aggregations.bucket.range.ParsedDateRange;
import org.opensearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;

@Service
public class RestaurantSearchService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantSearchService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public PagedResult<Restaurant> findByBorough(String query, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return new PagedResult<>(restaurantRepository.findByBorough(query, pageable));
    }

    public PagedResult<Restaurant> multiSearchQuery(
            String query, Integer offset, Integer limit, Boolean prefixPhraseEnabled) {
        Pageable pageable = PageRequest.of(offset, limit);
        return restaurantRepository.findByBoroughOrCuisineOrName(query, prefixPhraseEnabled, pageable);
    }

    public PagedResult<Restaurant> termQueryForBorough(String query, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return restaurantRepository.termQueryForBorough(query, pageable);
    }

    public PagedResult<Restaurant> termsQueryForBorough(List<String> queries, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return restaurantRepository.termsQueryForBorough(queries, pageable);
    }

    public PagedResult<Restaurant> queryBoolWithMust(
            String borough, String cuisine, String name, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return new PagedResult<>(restaurantRepository.findByBoroughAndCuisineAndName(borough, cuisine, name, pageable));
    }

    public PagedResult<Restaurant> queryBoolWithShould(
            String borough, String cuisine, String name, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return restaurantRepository.queryBoolWithShould(borough, cuisine, name, pageable);
    }

    public PagedResult<Restaurant> wildcardSearch(String queryKeyword, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return restaurantRepository.wildcardSearch(queryKeyword, pageable);
    }

    public PagedResult<Restaurant> regExpSearch(String queryKeyword, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return restaurantRepository.regExpSearch(queryKeyword, pageable);
    }

    public PagedResult<Restaurant> searchSimpleQueryForBoroughAndCuisine(
            String queryKeyword, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return restaurantRepository.searchSimpleQueryForBoroughAndCuisine(queryKeyword, pageable);
    }

    public PagedResult<Restaurant> searchRestaurantIdRange(
            Long lowerLimit, Long upperLimit, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return restaurantRepository.searchRestaurantIdRange(lowerLimit, upperLimit, pageable);
    }

    public PagedResult<Restaurant> searchDateRange(String fromDate, String toDate, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return restaurantRepository.searchDateRange(fromDate, toDate, pageable);
    }

    public PagedResult<Restaurant> aggregateSearch(
            String searchKeyword,
            List<String> fieldNames,
            String sortOrder,
            Integer limit,
            Integer offset,
            String... sortFields) {
        Sort.Direction direction;
        if (StringUtils.endsWithIgnoreCase(sortOrder, "ASC")) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }

        SearchPage<Restaurant> searchPage =
                restaurantRepository.aggregateSearch(searchKeyword, fieldNames, direction, limit, offset, sortFields);

        OpenSearchAggregations elasticsearchAggregations =
                (OpenSearchAggregations) searchPage.getSearchHits().getAggregations();
        Map<String, Map<String, Long>> map = new HashMap<>();
        if (elasticsearchAggregations != null) {
            map = aggregationFunction.apply(
                    elasticsearchAggregations.aggregations().asMap());
        }
        return new PagedResult<>(searchPage, map);
    }

    final Function<Map<String, Aggregation>, Map<String, Map<String, Long>>> aggregationFunction = aggregationMap -> {
        Map<String, Map<String, Long>> resultMap = new HashMap<>();
        aggregationMap.forEach((String aggregateKey, Aggregation aggregation) -> {
            Map<String, Long> countMap = new HashMap<>();

            if (aggregation instanceof ParsedStringTerms parsedStringTerms) {

                countMap = parsedStringTerms.getBuckets().stream()
                        .collect(Collectors.toMap(
                                MultiBucketsAggregation.Bucket::getKeyAsString,
                                MultiBucketsAggregation.Bucket::getDocCount));
            } else if (aggregation instanceof ParsedDateRange parsedDateRange) {
                countMap = parsedDateRange.getBuckets().stream()
                        .filter(bucket -> bucket.getDocCount() != 0)
                        .collect(Collectors.toMap(
                                bucket -> bucket.getFromAsString() + " - " + bucket.getToAsString(),
                                MultiBucketsAggregation.Bucket::getDocCount));
            }
            resultMap.put(aggregateKey, countMap);
        });

        return resultMap;
    };

    public List<ResultData> searchRestaurantsWithInRange(Double lat, Double lon, Double distance, String unit) {
        GeoPoint location = new GeoPoint(lat, lon);
        return this.restaurantRepository.searchWithin(location, distance, unit).stream()
                .map(restaurantSearchHit -> {
                    Double dist = (Double) restaurantSearchHit.getSortValues().getFirst();
                    Restaurant eRestaurant = restaurantSearchHit.getContent();
                    return new ResultData(
                            eRestaurant.getName(), eRestaurant.getAddress().getLocation(), dist);
                })
                .toList();
    }
}
