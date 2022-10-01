package com.example.mongoes.elasticsearch.repository;

import com.example.mongoes.document.Restaurant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.GeoDistanceOrder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CustomRestaurantESRepositoryImpl implements CustomRestaurantESRepository {

    private static final int PAGE_SIZE = 10_000;
    private final ReactiveElasticsearchOperations reactiveElasticsearchOperations;

    @Override
    public Flux<SearchHit<Restaurant>> searchWithin(
            GeoPoint geoPoint, Double distance, String unit) {

        Query query =
                new CriteriaQuery(
                        new Criteria("address.coord").within(geoPoint, distance.toString() + unit));

        // add a sort to get the actual distance back in the sort value
        Sort sort = Sort.by(new GeoDistanceOrder("address.coord", geoPoint).withUnit(unit));
        query.addSort(sort);

        return reactiveElasticsearchOperations.search(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> findByBoroughOrCuisineOrName(
            String queryKeyWord, Boolean prefixPhraseEnabled, Pageable pageable) {
        MultiMatchQueryBuilder multiMatchQuery =
                QueryBuilders.multiMatchQuery(queryKeyWord, "borough", "cuisine", "name");
        if (prefixPhraseEnabled) {
            multiMatchQuery.type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX);
        }

        Query query = new NativeSearchQuery(multiMatchQuery);
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> queryBoroughKeywordTerm(String keyword, Pageable pageable) {
        Query query = new NativeSearchQuery(QueryBuilders.termQuery("borough.keyword", keyword));
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> termQueryForBorough(String queryTerm, Pageable pageable) {
        Query query =
                new NativeSearchQuery(QueryBuilders.termQuery("borough", queryTerm.toLowerCase()));
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> termsQueryForBorough(
            List<String> queries, Pageable pageable) {
        Query query =
                new NativeSearchQuery(
                        QueryBuilders.termsQuery(
                                "borough", queries.stream().map(String::toLowerCase).toList()));
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> queryBoolWithShould(
            String borough, String cuisine, String name, Pageable pageable) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder queryBuilders = new BoolQueryBuilder();
        queryBuilders.should(QueryBuilders.matchQuery("borough", borough));
        queryBuilders.should(QueryBuilders.wildcardQuery("cuisine", "*" + cuisine + "*"));
        queryBuilders.should(QueryBuilders.matchQuery("name", name));
        sourceBuilder.query(queryBuilders);
        Query query = new NativeSearchQuery(sourceBuilder.query());
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> wildcardSearch(String queryKeyword, Pageable pageable) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder queryBuilders = new BoolQueryBuilder();
        queryBuilders.should(QueryBuilders.wildcardQuery("borough", "*" + queryKeyword + "*"));
        queryBuilders.should(QueryBuilders.wildcardQuery("cuisine", "*" + queryKeyword + "*"));
        queryBuilders.should(QueryBuilders.wildcardQuery("name", "*" + queryKeyword + "*"));
        sourceBuilder.query(queryBuilders);
        Query query = new NativeSearchQuery(sourceBuilder.query());
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> regExpSearch(String queryKeyword, Pageable pageable) {
        Query query = new NativeSearchQuery(QueryBuilders.regexpQuery("borough", "e[a-z]*h"));
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> searchSimpleQueryForBoroughAndCuisine(
            String queryKeyword, Pageable pageable) {
        Map<String, Float> map = new HashMap<>();
        map.put("borough", 1.0F);
        map.put("cuisine", 2.0F);
        Query query =
                new NativeSearchQuery(
                        QueryBuilders.simpleQueryStringQuery(queryKeyword).fields(map));
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> searchRestaurantIdRange(
            Long lowerLimit, Long upperLimit, Pageable pageable) {
        Query query =
                new NativeSearchQuery(
                        QueryBuilders.rangeQuery("restaurant_id").gte(lowerLimit).lte(upperLimit));
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> searchDateRange(
            String fromDate, String toDate, Pageable pageable) {
        Query query =
                new NativeSearchQuery(
                        QueryBuilders.rangeQuery("grades.date").gte(fromDate).lte(toDate));
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> aggregateSearch(
            String searchKeyword,
            List<String> fieldNames,
            Sort.Direction direction,
            Integer limit,
            Integer offset,
            String[] sortFields) {
        TermsAggregationBuilder cuisineTermsBuilder =
                AggregationBuilders.terms("MyCuisine")
                        .field("cuisine")
                        .size(PAGE_SIZE)
                        .order(BucketOrder.count(false));
        TermsAggregationBuilder boroughTermsBuilder =
                AggregationBuilders.terms("MyBorough")
                        .field("borough")
                        .size(PAGE_SIZE)
                        .order(BucketOrder.count(false));
        DateRangeAggregationBuilder dateRangeBuilder =
                AggregationBuilders.dateRange("MyDateRange").field("grades.date");
        addDateRange(dateRangeBuilder);

        Query query =
                new NativeSearchQueryBuilder()
                        .withQuery(
                                QueryBuilders.multiMatchQuery(
                                                searchKeyword, fieldNames.toArray(String[]::new))
                                        .operator(Operator.OR))
                        .withSort(Sort.by(direction, sortFields))
                        .withAggregations(
                                cuisineTermsBuilder, boroughTermsBuilder, dateRangeBuilder)
                        .build();
        query.setPageable(PageRequest.of(offset, limit));

        return this.reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> findAll(Pageable pageable) {
        Query query = new CriteriaQuery(Criteria.where("_id").exists());
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    private void addDateRange(DateRangeAggregationBuilder dateRangeBuilder) {
        ZonedDateTime zonedDateTime =
                ZonedDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay(ZoneId.of("UTC"));
        dateRangeBuilder.addUnboundedTo(zonedDateTime.minusMonths(12));
        for (int i = 12; i > 0; i--) {
            dateRangeBuilder.addRange(
                    zonedDateTime.minusMonths(i), zonedDateTime.minusMonths(i - 1).minusSeconds(1));
        }
        dateRangeBuilder.addUnboundedFrom(zonedDateTime);
    }
}
