package com.example.mongoes.web.controller;

import com.example.mongoes.document.Restaurant;
import com.example.mongoes.model.response.ResultData;
import com.example.mongoes.model.response.SearchPageResponse;
import com.example.mongoes.web.api.SearchApi;
import com.example.mongoes.web.service.SearchService;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Timed
@Validated
class SearchController implements SearchApi {

    private final SearchService searchService;

    SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public Mono<ResponseEntity<Flux<Restaurant>>> searchPhrase(
            String query, Integer limit, Integer offset) {
        return searchService.searchMatchBorough(query, offset, limit).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<SearchPageResponse<Restaurant>>> searchMulti(
            String query, Integer limit, Integer offset, Boolean prefixPhraseEnabled) {
        return searchService
                .multiSearchQuery(query, offset, limit, prefixPhraseEnabled)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<SearchPageResponse<Restaurant>>> searchTermForBorough(
            String query, Integer limit, Integer offset) {
        return searchService.termQueryForBorough(query, offset, limit).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<SearchPageResponse<Restaurant>>> searchTerms(
            List<String> queries, Integer limit, Integer offset) {
        return searchService.termsQueryForBorough(queries, offset, limit).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Flux<Restaurant>>> searchBoolMust(
            String borough, String cuisine, String name, Integer limit, Integer offset) {
        return searchService
                .queryBoolWithMust(borough, cuisine, name, offset, limit)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<SearchPageResponse<Restaurant>>> searchBoolShould(
            String borough, String cuisine, String name, Integer limit, Integer offset) {
        return searchService
                .queryBoolWithShould(borough, cuisine, name, offset, limit)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<SearchPageResponse<Restaurant>>> searchWildcard(
            String query, Integer limit, Integer offset) {
        return searchService.wildcardSearch(query, offset, limit).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<SearchPageResponse<Restaurant>>> searchRegularExpression(
            String query, Integer limit, Integer offset) {
        return searchService.regExpSearch(query, offset, limit).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<SearchPageResponse<Restaurant>>>
            searchSimpleQueryForBoroughAndCuisine(String query, Integer limit, Integer offset) {
        return searchService
                .searchSimpleQueryForBoroughAndCuisine(query, offset, limit)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<SearchPageResponse<Restaurant>>> searchRestaurantIdRange(
            Long lowerLimit, Long upperLimit, Integer limit, Integer offset) {
        return searchService
                .searchRestaurantIdRange(lowerLimit, upperLimit, offset, limit)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<SearchPageResponse<Restaurant>>> searchDateRange(
            String fromDate, String toDate, Integer limit, Integer offset) {
        return searchService
                .searchDateRange(fromDate, toDate, offset, limit)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<SearchPageResponse<Restaurant>>> aggregateSearch(
            String searchKeyword,
            List<String> fieldNames,
            Integer limit,
            Integer offset,
            String sortOrder,
            String... sortFields) {
        return searchService
                .aggregateSearch(searchKeyword, fieldNames, sortOrder, limit, offset, sortFields)
                .map(ResponseEntity::ok);
    }

    @Override
    public Flux<ResultData> searchRestaurantsWithInRange(
            Double lat, Double lon, Double distance, String unit) {
        return this.searchService.searchRestaurantsWithInRange(lat, lon, distance, unit);
    }
}
