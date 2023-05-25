package com.example.mongoes.web.controller;

import com.example.mongoes.document.Restaurant;
import com.example.mongoes.response.AggregationSearchResponse;
import com.example.mongoes.response.ResultData;
import com.example.mongoes.web.service.SearchService;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Timed
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search/borough")
    public Mono<ResponseEntity<Flux<Restaurant>>> searchPhrase(
            @RequestParam("query") String query,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return searchService.searchMatchBorough(query, offset, limit).map(ResponseEntity::ok);
    }

    @GetMapping("/search/multi")
    public Mono<ResponseEntity<SearchPage<Restaurant>>> searchMulti(
            @RequestParam("query") String query,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "prefix_phrase_enabled", defaultValue = "false")
                    Boolean prefixPhraseEnabled) {
        return searchService
                .multiSearchQuery(query, offset, limit, prefixPhraseEnabled)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/search/term/borough")
    public Mono<ResponseEntity<SearchPage<Restaurant>>> searchTermForBorough(
            @RequestParam("query") String query,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return searchService.termQueryForBorough(query, offset, limit).map(ResponseEntity::ok);
    }

    @GetMapping("/search/terms")
    public Mono<ResponseEntity<SearchPage<Restaurant>>> searchTerms(
            @RequestParam("query") List<String> queries,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return searchService.termsQueryForBorough(queries, offset, limit).map(ResponseEntity::ok);
    }

    @GetMapping("/search/must/bool")
    public Mono<ResponseEntity<Flux<Restaurant>>> searchBoolMust(
            @RequestParam("borough") String borough,
            @RequestParam("cuisine") String cuisine,
            @RequestParam("name") String name,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return searchService
                .queryBoolWithMust(borough, cuisine, name, offset, limit)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/search/should/bool")
    public Mono<ResponseEntity<SearchPage<Restaurant>>> searchBoolShould(
            @RequestParam("borough") String borough,
            @RequestParam("cuisine") String cuisine,
            @RequestParam("name") String name,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return searchService
                .queryBoolWithShould(borough, cuisine, name, offset, limit)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/search/wildcard/borough")
    public Mono<ResponseEntity<SearchPage<Restaurant>>> searchBoolShould(
            @RequestParam("query") String query,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return searchService.wildcardSearch(query, offset, limit).map(ResponseEntity::ok);
    }

    @GetMapping("/search/regexp/borough")
    public Mono<ResponseEntity<SearchPage<Restaurant>>> searchRegularExpression(
            @RequestParam("query") String query,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return searchService.regExpSearch(query, offset, limit).map(ResponseEntity::ok);
    }

    @GetMapping("/search/simple")
    public Mono<ResponseEntity<SearchPage<Restaurant>>> searchSimpleQueryForBoroughAndCuisine(
            @RequestParam("query") String query,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return searchService
                .searchSimpleQueryForBoroughAndCuisine(query, offset, limit)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/search/restaurant/range")
    public Mono<ResponseEntity<SearchPage<Restaurant>>> searchRestaurantIdRange(
            @RequestParam("lowerLimit") Long lowerLimit,
            @RequestParam("upperLimit") Long upperLimit,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return searchService
                .searchRestaurantIdRange(lowerLimit, upperLimit, offset, limit)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/search/date/range")
    public Mono<ResponseEntity<SearchPage<Restaurant>>> searchDateRange(
            @RequestParam("fromDate") String fromDate,
            @RequestParam("toDate") String toDate,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return searchService
                .searchDateRange(fromDate, toDate, offset, limit)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/search/aggregate")
    public Mono<ResponseEntity<AggregationSearchResponse>> aggregateSearch(
            @RequestParam(name = "searchKeyword") String searchKeyword,
            @RequestParam(name = "fieldNames") List<String> fieldNames,
            @RequestParam(required = false, name = "limit", defaultValue = "15") Integer limit,
            @RequestParam(required = false, name = "offset", defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "DESC", name = "sortOrder")
                    String sortOrder,
            @RequestParam(required = false, defaultValue = "restaurant_id", name = "sortFields")
                    String... sortFields) {
        return searchService
                .aggregateSearch(searchKeyword, fieldNames, sortOrder, limit, offset, sortFields)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/search/restaurant/withInRange")
    public Flux<ResultData> searchRestaurantsWithInRange(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam Double distance,
            @RequestParam(defaultValue = "km", required = false) String unit) {
        return this.searchService.searchRestaurantsWithInRange(lat, lon, distance, unit);
    }
}
