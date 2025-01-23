package com.example.mongoes.web.service;

import com.example.mongoes.document.Restaurant;
import com.example.mongoes.model.response.AggregationSearchResponse;
import com.example.mongoes.model.response.ResultData;
import com.example.mongoes.repository.elasticsearch.RestaurantESRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SearchService {

    private final RestaurantESRepository restaurantESRepository;
    private final AggregationProcessor aggregationProcessor;

    public SearchService(
            RestaurantESRepository restaurantESRepository,
            AggregationProcessor aggregationProcessor) {
        this.restaurantESRepository = restaurantESRepository;
        this.aggregationProcessor = aggregationProcessor;
    }

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
                                        aggregationProcessor.processAggregations(
                                                elasticsearchAggregations.aggregationsAsMap());
                            }
                            return new AggregationSearchResponse(
                                    searchPage.getContent().stream()
                                            .map(SearchHit::getContent)
                                            .toList(),
                                    map,
                                    searchPage.getPageable(),
                                    searchPage.getTotalPages(),
                                    searchPage.getTotalElements());
                        });
    }

    public Flux<ResultData> searchRestaurantsWithInRange(
            Double lat, Double lon, Double distance, String unit) {
        GeoPoint location = new GeoPoint(lat, lon);
        return this.restaurantESRepository
                .searchWithin(location, distance, unit)
                .flatMap(
                        restaurantSearchHit -> {
                            Double dist = (Double) restaurantSearchHit.getSortValues().getFirst();
                            Restaurant eRestaurant = restaurantSearchHit.getContent();
                            return Mono.just(
                                    new ResultData(
                                            eRestaurant.getName(),
                                            eRestaurant.getAddress().getLocation(),
                                            dist));
                        });
    }
}
