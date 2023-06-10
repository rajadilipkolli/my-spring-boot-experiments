package com.example.mongoes.web.service;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.RangeBucket;
import com.example.mongoes.document.Restaurant;
import com.example.mongoes.elasticsearch.repository.RestaurantESRepository;
import com.example.mongoes.response.AggregationSearchResponse;
import com.example.mongoes.response.ResultData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final RestaurantESRepository restaurantESRepository;

    public Mono<Flux<Restaurant>> searchMatchBorough(String query, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return Mono.just(restaurantESRepository.findByBorough(query, pageable));
    }

    public Mono<SearchPage<Restaurant>> multiSearchQuery(
            String query, Integer offset, Integer limit, Boolean prefixPhraseEnabled) {
        Pageable pageable = PageRequest.of(offset, limit);
        return restaurantESRepository.findByBoroughOrCuisineOrName(
                query, prefixPhraseEnabled, pageable);
    }

    public Mono<SearchPage<Restaurant>> termQueryForBorough(
            String query, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return restaurantESRepository.termQueryForBorough(query, pageable);
    }

    public Mono<SearchPage<Restaurant>> termsQueryForBorough(
            List<String> queries, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return restaurantESRepository.termsQueryForBorough(queries, pageable);
    }

    public Mono<Flux<Restaurant>> queryBoolWithMust(
            String borough, String cuisine, String name, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return Mono.just(
                restaurantESRepository.findByBoroughAndCuisineAndName(
                        borough, cuisine, name, pageable));
    }

    public Mono<SearchPage<Restaurant>> queryBoolWithShould(
            String borough, String cuisine, String name, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return restaurantESRepository.queryBoolWithShould(borough, cuisine, name, pageable);
    }

    public Mono<SearchPage<Restaurant>> wildcardSearch(
            String queryKeyword, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return restaurantESRepository.wildcardSearch(queryKeyword, pageable);
    }

    public Mono<SearchPage<Restaurant>> regExpSearch(
            String queryKeyword, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return restaurantESRepository.regExpSearch(queryKeyword, pageable);
    }

    public Mono<SearchPage<Restaurant>> searchSimpleQueryForBoroughAndCuisine(
            String queryKeyword, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return restaurantESRepository.searchSimpleQueryForBoroughAndCuisine(queryKeyword, pageable);
    }

    public Mono<SearchPage<Restaurant>> searchRestaurantIdRange(
            Long lowerLimit, Long upperLimit, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return restaurantESRepository.searchRestaurantIdRange(lowerLimit, upperLimit, pageable);
    }

    public Mono<SearchPage<Restaurant>> searchDateRange(
            String fromDate, String toDate, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return restaurantESRepository.searchDateRange(fromDate, toDate, pageable);
    }

    public Mono<AggregationSearchResponse> aggregateSearch(
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

        return restaurantESRepository
                .aggregateSearch(searchKeyword, fieldNames, direction, limit, offset, sortFields)
                .map(
                        searchPage -> {
                            ElasticsearchAggregations elasticsearchAggregations =
                                    (ElasticsearchAggregations)
                                            searchPage.getSearchHits().getAggregations();
                            Map<String, Map<String, Long>> map = new HashMap<>();
                            if (elasticsearchAggregations != null) {
                                map =
                                        aggregationFunction.apply(
                                                elasticsearchAggregations.aggregationsAsMap());
                            }
                            return new AggregationSearchResponse(
                                    searchPage.getContent(),
                                    map,
                                    searchPage.getPageable(),
                                    searchPage.getTotalPages(),
                                    searchPage.getTotalElements());
                        });
    }

    Function<Map<String, ElasticsearchAggregation>, Map<String, Map<String, Long>>>
            aggregationFunction =
                    aggregationMap -> {
                        Map<String, Map<String, Long>> resultMap = new HashMap<>();
                        aggregationMap.forEach(
                                (String aggregateKey, ElasticsearchAggregation aggregation) -> {
                                    Map<String, Long> countMap = new HashMap<>();
                                    Aggregate aggregate = aggregation.aggregation().getAggregate();
                                    if (aggregate.isSterms()) {
                                        aggregate
                                                .sterms()
                                                .buckets()
                                                .array()
                                                .forEach(
                                                        stringTermsBucket ->
                                                                countMap.put(
                                                                        stringTermsBucket
                                                                                .key()
                                                                                .stringValue(),
                                                                        stringTermsBucket
                                                                                .docCount()));
                                    } else if (aggregate.isDateRange()) {
                                        List<RangeBucket> bucketList =
                                                aggregate.dateRange().buckets().array();
                                        bucketList.forEach(
                                                rangeBucket -> {
                                                    if (rangeBucket.docCount() != 0) {
                                                        countMap.put(
                                                                rangeBucket.fromAsString()
                                                                        + " - "
                                                                        + rangeBucket.toAsString(),
                                                                rangeBucket.docCount());
                                                    }
                                                });
                                    }
                                    resultMap.put(aggregateKey, countMap);
                                });

                        return resultMap;
                    };

    public Flux<ResultData> searchRestaurantsWithInRange(
            Double lat, Double lon, Double distance, String unit) {
        GeoPoint location = new GeoPoint(lat, lon);
        return this.restaurantESRepository
                .searchWithin(location, distance, unit)
                .flatMap(
                        restaurantSearchHit -> {
                            Double dist = (Double) restaurantSearchHit.getSortValues().get(0);
                            Restaurant eRestaurant = restaurantSearchHit.getContent();
                            return Mono.just(
                                    new ResultData(
                                            eRestaurant.getName(),
                                            eRestaurant.getAddress().getLocation(),
                                            dist));
                        });
    }
}
