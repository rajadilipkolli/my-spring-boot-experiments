package com.example.opensearch.web.controllers;

import com.example.opensearch.entities.Restaurant;
import com.example.opensearch.model.response.PagedResult;
import com.example.opensearch.model.response.ResultData;
import com.example.opensearch.services.RestaurantSearchService;
import io.micrometer.core.annotation.Timed;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Timed
@RestController
public class SearchController {

    private final RestaurantSearchService searchService;

    public SearchController(RestaurantSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search/borough")
    public ResponseEntity<PagedResult<Restaurant>> searchPhrase(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {
        return ResponseEntity.ok(searchService.findByBorough(query, offset, limit));
    }

    @GetMapping("/search/multi")
    public ResponseEntity<PagedResult<Restaurant>> searchMulti(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(value = "prefix_phrase_enabled", defaultValue = "false") Boolean prefixPhraseEnabled) {
        return ResponseEntity.ok(searchService.multiSearchQuery(query, offset, limit, prefixPhraseEnabled));
    }

    @GetMapping("/search/term/borough")
    public ResponseEntity<PagedResult<Restaurant>> searchTermForBorough(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {
        return ResponseEntity.ok(searchService.termQueryForBorough(query, offset, limit));
    }

    @GetMapping("/search/terms")
    public ResponseEntity<PagedResult<Restaurant>> searchTerms(
            @RequestParam("query") List<String> queries,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {
        return ResponseEntity.ok(searchService.termsQueryForBorough(queries, offset, limit));
    }

    @GetMapping("/search/must/bool")
    public ResponseEntity<PagedResult<Restaurant>> queryBoolWithMust(
            @RequestParam String borough,
            @RequestParam String cuisine,
            @RequestParam String name,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {
        return ResponseEntity.ok(searchService.queryBoolWithMust(borough, cuisine, name, offset, limit));
    }

    @GetMapping("/search/should/bool")
    public ResponseEntity<PagedResult<Restaurant>> searchBoolShould(
            @RequestParam String borough,
            @RequestParam String cuisine,
            @RequestParam String name,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {
        return ResponseEntity.ok(searchService.queryBoolWithShould(borough, cuisine, name, offset, limit));
    }

    @GetMapping("/search/wildcard/borough")
    public ResponseEntity<PagedResult<Restaurant>> searchWildCardBorough(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {
        return ResponseEntity.ok(searchService.wildcardSearch(query, offset, limit));
    }

    @GetMapping("/search/regexp/borough")
    public ResponseEntity<PagedResult<Restaurant>> searchRegularExpression(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {
        return ResponseEntity.ok(searchService.regExpSearch(query, offset, limit));
    }

    @GetMapping("/search/simple")
    public ResponseEntity<PagedResult<Restaurant>> searchSimpleQueryForBoroughAndCuisine(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {
        return ResponseEntity.ok(searchService.searchSimpleQueryForBoroughAndCuisine(query, offset, limit));
    }

    @GetMapping("/search/restaurant/range")
    public ResponseEntity<PagedResult<Restaurant>> searchRestaurantIdRange(
            @RequestParam Long lowerLimit,
            @RequestParam Long upperLimit,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {
        return ResponseEntity.ok(searchService.searchRestaurantIdRange(lowerLimit, upperLimit, offset, limit));
    }

    @GetMapping("/search/date/range")
    public ResponseEntity<PagedResult<Restaurant>> searchDateRange(
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {
        return ResponseEntity.ok(searchService.searchDateRange(fromDate, toDate, offset, limit));
    }

    @GetMapping("/search/aggregate")
    public ResponseEntity<PagedResult<Restaurant>> aggregateSearch(
            @RequestParam String searchKeyword,
            @RequestParam List<String> fieldNames,
            @RequestParam(required = false, defaultValue = "15") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "DESC") String sortOrder,
            @RequestParam(required = false, defaultValue = "id") String... sortFields) {
        return ResponseEntity.ok(
                searchService.aggregateSearch(searchKeyword, fieldNames, sortOrder, limit, offset, sortFields));
    }

    @GetMapping("/search/restaurant/withInRange")
    public ResponseEntity<List<ResultData>> searchRestaurantsWithInRange(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam Double distance,
            @RequestParam(defaultValue = "km", required = false) String unit) {
        return ResponseEntity.ok(searchService.searchRestaurantsWithInRange(lat, lon, distance, unit));
    }
}
