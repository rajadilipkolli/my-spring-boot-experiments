package com.example.mongoes.web.service;

import com.example.mongoes.document.Restaurant;
import com.example.mongoes.elasticsearch.repository.RestaurantESRepository;
import com.example.mongoes.response.AggregationSearchResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.range.ParsedDateRange;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
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

        NativeSearchQueryBuilder nativeSearchQueryBuilder =
                new NativeSearchQueryBuilder()
                        .withQuery(
                                QueryBuilders.multiMatchQuery(
                                                searchKeyword, fieldNames.toArray(String[]::new))
                                        .operator(Operator.OR))
                        .withSort(Sort.by(direction, sortFields));

        return Mono.zip(
                        restaurantESRepository.searchResultsForFacets(
                                nativeSearchQueryBuilder, limit, offset),
                        restaurantESRepository
                                .aggregateSearch(nativeSearchQueryBuilder)
                                .map(
                                        aggregationContainer -> {
                                            Aggregation aggregation =
                                                    (Aggregation)
                                                            aggregationContainer.aggregation();
                                            List<?> buckets;
                                            if (aggregation
                                                    instanceof
                                                    ParsedStringTerms
                                                    parsedStringTerms) {
                                                buckets = parsedStringTerms.getBuckets();
                                            } else if (aggregation
                                                    instanceof ParsedLongTerms parsedLongTerms) {
                                                buckets = parsedLongTerms.getBuckets();
                                            } else if (aggregation
                                                    instanceof ParsedDateRange parsedDateRange) {
                                                buckets = parsedDateRange.getBuckets();
                                            } else {
                                                throw new UnsupportedOperationException(
                                                        "Unsupported type ("
                                                                + aggregation.getClass()
                                                                + ") for aggList:"
                                                                + aggregation.getName());
                                            }
                                            Map<String, Map<String, Object>> resultMap =
                                                    new HashMap<>();
                                            Map<String, Object> subItems = new HashMap<>();
                                            for (Object resultItemObj : buckets) {
                                                String key = "";
                                                long count = 0;
                                                if (resultItemObj
                                                        instanceof Terms.Bucket resultItem) {
                                                    key = resultItem.getKeyAsString();
                                                    count = resultItem.getDocCount();
                                                } else if (resultItemObj
                                                        instanceof
                                                        ParsedDateRange.ParsedBucket
                                                        resultItem) {
                                                    String from = resultItem.getFromAsString();
                                                    String to = resultItem.getToAsString();
                                                    key = from + " - " + to;
                                                    count = resultItem.getDocCount();
                                                }
                                                if (count != 0) {
                                                    subItems.put(key, count);
                                                }
                                            }
                                            resultMap.put(aggregation.getName(), subItems);
                                            return resultMap;
                                        })
                                .collectList())
                .map(
                        objects ->
                                new AggregationSearchResponse(
                                        objects.getT1(),
                                        objects.getT2(),
                                        objects.getT1().getSearchHits().getTotalHits()));
    }
}
