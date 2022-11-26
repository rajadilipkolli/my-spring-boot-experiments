package com.example.mongoes.elasticsearch.repository;

import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
import co.elastic.clients.elasticsearch._types.aggregations.DateRangeAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.DateRangeAggregation.Builder;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.json.JsonData;
import com.example.mongoes.document.Restaurant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.GeoDistanceOrder;
import org.springframework.data.elasticsearch.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CustomRestaurantESRepositoryImpl implements CustomRestaurantESRepository {

    private static final int PAGE_SIZE = 1_000;
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
        MultiMatchQuery.Builder builder =
                QueryBuilders.multiMatch().query(queryKeyWord).fields("borough", "cuisine", "name");
        if (prefixPhraseEnabled) {
            builder.type(TextQueryType.PhrasePrefix);
        }

        Query query = new NativeQuery(builder.build()._toQuery());
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> termQueryForBorough(String queryTerm, Pageable pageable) {
        Query query =
                new NativeQuery(
                        QueryBuilders.term()
                                .field("borough")
                                .value(queryTerm)
                                .caseInsensitive(true)
                                .build()
                                ._toQuery());
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> termsQueryForBorough(
            List<String> queries, Pageable pageable) {
        Query query =
                // new NativeSearchQuery(
                //         QueryBuilders.termsQuery(
                //                 "borough", queries.stream().map(String::toLowerCase).toList()));
                new NativeQuery(
                        QueryBuilders.multiMatch()
                                .fields("borough")
                                .query(queries.get(0))
                                .build()
                                ._toQuery());
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> queryBoolWithShould(
            String borough, String cuisine, String name, Pageable pageable) {
        BoolQuery.Builder boolQueryBuilder = QueryBuilders.bool();
        boolQueryBuilder.should(
                QueryBuilders.match().field("borough").query(borough).build()._toQuery());
        boolQueryBuilder.should(
                QueryBuilders.wildcard()
                        .field("cuisine")
                        .value("*" + cuisine + "*")
                        .build()
                        ._toQuery());
        boolQueryBuilder.should(QueryBuilders.match().field("name").query(name).build()._toQuery());
        Query query = new NativeQuery(boolQueryBuilder.build()._toQuery());
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> wildcardSearch(String queryKeyword, Pageable pageable) {
        BoolQuery.Builder queryBuilders = QueryBuilders.bool();
        queryBuilders.should(
                QueryBuilders.wildcard()
                        .field("borough")
                        .value("*" + queryKeyword + "*")
                        .build()
                        ._toQuery());
        queryBuilders.should(
                QueryBuilders.wildcard()
                        .field("cuisine")
                        .value("*" + queryKeyword + "*")
                        .build()
                        ._toQuery());
        queryBuilders.should(
                QueryBuilders.wildcard()
                        .field("name")
                        .value("*" + queryKeyword + "*")
                        .build()
                        ._toQuery());
        Query query = new NativeQuery(queryBuilders.build()._toQuery());
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> regExpSearch(String reqEx, Pageable pageable) {
        Query query =
                new NativeQuery(
                        QueryBuilders.regexp()
                                .field("borough")
                                .value(reqEx)
                                .caseInsensitive(true)
                                .build()
                                ._toQuery());
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
                new NativeQuery(
                        QueryBuilders.simpleQueryString()
                                .query(queryKeyword)
                                .fields("borough", "cuisine")
                                .build()
                                ._toQuery());
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> searchRestaurantIdRange(
            Long lowerLimit, Long upperLimit, Pageable pageable) {
        Query query =
                new NativeQuery(
                        QueryBuilders.range()
                                .field("restaurant_id")
                                .gte(JsonData.of(lowerLimit))
                                .lte(JsonData.of(upperLimit))
                                .build()
                                ._toQuery());
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> searchDateRange(
            String fromDate, String toDate, Pageable pageable) {
        Query query =
                new NativeQuery(
                        QueryBuilders.range()
                                .field("grades.date")
                                .gte(JsonData.of(fromDate))
                                .lte(JsonData.of(toDate))
                                .build()
                                ._toQuery());
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
        TermsAggregation cuisineTermsBuilder =
                AggregationBuilders.terms()
                        .name("MyCuisine")
                        .field("cuisine")
                        .size(PAGE_SIZE)
                        .build();
        // .order(BucketOrder.count(false));
        TermsAggregation boroughTermsBuilder =
                AggregationBuilders.terms()
                        .name("MyBorough")
                        .field("borough")
                        .size(PAGE_SIZE)
                        .build();
        // .order(BucketOrder.count(false));
        Builder dateRangeBuilder =
                AggregationBuilders.dateRange().name("MyDateRange").field("grades.date");
        addDateRange(dateRangeBuilder);

        Query query =
                new NativeQueryBuilder()
                        .withQuery(
                                QueryBuilders.multiMatch()
                                        .query(searchKeyword)
                                        .fields(fieldNames)
                                        .operator(Operator.Or)
                                        .build()
                                        ._toQuery())
                        .withSort(Sort.by(direction, sortFields))
                        .withAggregation("MyBorough", boroughTermsBuilder._toAggregation())
                        .withAggregation("MyCuisine", cuisineTermsBuilder._toAggregation())
                        // .withAggregations(
                        //         cuisineTermsBuilder, boroughTermsBuilder, dateRangeBuilder)
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

    private void addDateRange(DateRangeAggregation.Builder dateRangeBuilder) {
        ZonedDateTime zonedDateTime =
                ZonedDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay(ZoneId.of("UTC"));
        // dateRangeBuilder.addUnboundedTo(zonedDateTime.minusMonths(12));
        // for (int i = 12; i > 0; i--) {
        //     dateRangeBuilder.addRange(
        //             zonedDateTime.minusMonths(i), zonedDateTime.minusMonths(i -
        // 1).minusSeconds(1));
        // }
        // dateRangeBuilder.addUnboundedFrom(zonedDateTime);
    }
}
