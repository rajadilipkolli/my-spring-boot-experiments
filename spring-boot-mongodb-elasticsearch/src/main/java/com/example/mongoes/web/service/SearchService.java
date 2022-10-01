package com.example.mongoes.web.service;

import com.example.mongoes.document.Restaurant;
import com.example.mongoes.elasticsearch.repository.RestaurantESRepository;
import com.example.mongoes.response.AggregationSearchResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.range.ParsedDateRange;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.core.SearchPage;
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

    public Mono<SearchPage<Restaurant>> queryBoroughKeywordTerm(
            String query, Integer offset, Integer limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return restaurantESRepository.queryBoroughKeywordTerm(query, pageable);
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
                                                elasticsearchAggregations.aggregations().asMap());
                            }
                            return new AggregationSearchResponse(
                                    searchPage.getContent(),
                                    map,
                                    searchPage.getPageable(),
                                    searchPage.getTotalPages(),
                                    searchPage.getTotalElements());
                        });
    }

    Function<Map<String, Aggregation>, Map<String, Map<String, Long>>> aggregationFunction =
            aggregationMap -> {
                Map<String, Map<String, Long>> resultMap = new HashMap<>();
                aggregationMap.forEach(
                        (String aggregateKey, Aggregation aggregation) -> {
                            Map<String, Long> countMap = new HashMap<>();
                            if (aggregation instanceof ParsedStringTerms parsedStringTerms) {
                                countMap =
                                        parsedStringTerms.getBuckets().stream()
                                                .collect(
                                                        Collectors.toMap(
                                                                MultiBucketsAggregation.Bucket
                                                                        ::getKeyAsString,
                                                                MultiBucketsAggregation.Bucket
                                                                        ::getDocCount));
                            } else if (aggregation instanceof ParsedDateRange parsedDateRange) {
                                countMap =
                                        parsedDateRange.getBuckets().stream()
                                                .filter(bucket -> bucket.getDocCount() != 0)
                                                .collect(
                                                        Collectors.toMap(
                                                                bucket ->
                                                                        bucket.getFromAsString()
                                                                                + " - "
                                                                                + bucket
                                                                                        .getToAsString(),
                                                                MultiBucketsAggregation.Bucket
                                                                        ::getDocCount));
                            }
                            resultMap.put(aggregateKey, countMap);
                        });

                return resultMap;
            };
}
