package com.example.mongoes.elasticsearch.repository;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
import co.elastic.clients.elasticsearch._types.aggregations.DateRangeExpression;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.json.JsonData;
import com.example.mongoes.document.Restaurant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
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

    private static final String BOROUGH = "borough";
    private static final String CUISINE = "cuisine";
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

        Query query =
                NativeQuery.builder()
                        .withQuery(
                                QueryBuilders.multiMatch(
                                        builder -> {
                                            builder.query(queryKeyWord)
                                                    .fields(BOROUGH, CUISINE, "name");
                                            if (prefixPhraseEnabled) {
                                                builder.type(TextQueryType.PhrasePrefix);
                                            }
                                            return builder;
                                        }))
                        .build();

        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> termQueryForBorough(String queryTerm, Pageable pageable) {
        Query query =
                NativeQuery.builder()
                        .withQuery(
                                QueryBuilders.term(
                                        builder ->
                                                builder.value(queryTerm)
                                                        .field(BOROUGH)
                                                        .caseInsensitive(true)))
                        .build();
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> termsQueryForBorough(
            List<String> queries, Pageable pageable) {
        Query query =
                NativeQuery.builder()
                        .withQuery(
                                QueryBuilders.terms(
                                        builder -> {
                                            builder.field(BOROUGH);
                                            builder.terms(
                                                    termsBuilder ->
                                                            termsBuilder.value(
                                                                    queries.stream()
                                                                            .map(
                                                                                    String
                                                                                            ::toLowerCase)
                                                                            .map(FieldValue::of)
                                                                            .toList()));
                                            return builder;
                                        }))
                        .build();
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> queryBoolWithShould(
            String borough, String cuisine, String name, Pageable pageable) {

        Query query =
                NativeQuery.builder()
                        .withQuery(
                                QueryBuilders.bool(
                                        builder ->
                                                builder.should(
                                                        QueryBuilders.match(
                                                                matchBuilder ->
                                                                        matchBuilder
                                                                                .field(BOROUGH)
                                                                                .query(borough)),
                                                        QueryBuilders.wildcard(
                                                                wildcardBuilder ->
                                                                        wildcardBuilder
                                                                                .field(CUISINE)
                                                                                .value(
                                                                                        "*"
                                                                                                + cuisine
                                                                                                + "*")),
                                                        QueryBuilders.match(
                                                                matchBuilder ->
                                                                        matchBuilder
                                                                                .field("name")
                                                                                .query(name)))))
                        .build();
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> wildcardSearch(String queryKeyword, Pageable pageable) {
        Query query =
                NativeQuery.builder()
                        .withQuery(
                                QueryBuilders.bool(
                                        builder ->
                                                builder.should(
                                                        QueryBuilders.wildcard(
                                                                boroughBuilder ->
                                                                        boroughBuilder
                                                                                .field(BOROUGH)
                                                                                .value(
                                                                                        "*"
                                                                                                + queryKeyword
                                                                                                + "*")),
                                                        QueryBuilders.wildcard(
                                                                boroughBuilder ->
                                                                        boroughBuilder
                                                                                .field(CUISINE)
                                                                                .value(
                                                                                        "*"
                                                                                                + queryKeyword
                                                                                                + "*")),
                                                        QueryBuilders.wildcard(
                                                                boroughBuilder ->
                                                                        boroughBuilder
                                                                                .field("name")
                                                                                .value(
                                                                                        "*"
                                                                                                + queryKeyword
                                                                                                + "*")))))
                        .build();
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> regExpSearch(String reqEx, Pageable pageable) {
        Query query =
                NativeQuery.builder()
                        .withQuery(
                                QueryBuilders.regexp(
                                        reqExBuilder ->
                                                reqExBuilder
                                                        .caseInsensitive(true)
                                                        .field(BOROUGH)
                                                        .value(reqEx)))
                        .build();
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> searchSimpleQueryForBoroughAndCuisine(
            String queryKeyword, Pageable pageable) {
        Query query =
                NativeQuery.builder()
                        .withQuery(
                                QueryBuilders.simpleQueryString(
                                        builder ->
                                                builder.query(queryKeyword)
                                                        .fields(BOROUGH)
                                                        .boost(1.0F)
                                                        .fields(CUISINE)
                                                        .boost(2.0F)))
                        .build();

        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> searchRestaurantIdRange(
            Long lowerLimit, Long upperLimit, Pageable pageable) {
        Query query =
                NativeQuery.builder()
                        .withQuery(
                                QueryBuilders.range(
                                        rangeBuilder ->
                                                rangeBuilder
                                                        .field("restaurant_id")
                                                        .lte(JsonData.of(upperLimit))
                                                        .gte(JsonData.of(lowerLimit))))
                        .build();
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    @Override
    public Mono<SearchPage<Restaurant>> searchDateRange(
            String fromDate, String toDate, Pageable pageable) {
        Query query =
                NativeQuery.builder()
                        .withQuery(
                                QueryBuilders.range(
                                        rangeBuilder ->
                                                rangeBuilder
                                                        .field("grades.date")
                                                        .lte(JsonData.of(toDate))
                                                        .gte(JsonData.of(fromDate))))
                        .build();
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
        Aggregation cuisineTermsBuilder =
                AggregationBuilders.terms(builder -> builder.field(CUISINE).size(PAGE_SIZE));
        // .order(BucketOrder.count(false));
        Aggregation boroughTermsBuilder =
                AggregationBuilders.terms(builder -> builder.field(CUISINE).size(PAGE_SIZE));
        // .order(BucketOrder.count(false));
        Aggregation dateRangeBuilder =
                AggregationBuilders.dateRange(
                        builder ->
                                builder.field("grades.date")
                                        .format("MM-yyyy")
                                        .ranges(
                                                DateRangeExpression.of(
                                                        dateRanageExpressionBuilder ->
                                                                dateRanageExpressionBuilder
                                                                        .key("Older")
                                                                        .to(
                                                                                builder1 ->
                                                                                        builder1
                                                                                                .expr(
                                                                                                        "now-120M/M"))),
                                                DateRangeExpression.of(
                                                        dateRanageExpressionBuilder ->
                                                                dateRanageExpressionBuilder
                                                                        .from(
                                                                                builder1 ->
                                                                                        builder1
                                                                                                .expr(
                                                                                                        "now-120M/M"))
                                                                        .to(
                                                                                builder1 ->
                                                                                        builder1
                                                                                                .expr(
                                                                                                        "now/M")))));
        //        addDateRange(dateRangeBuilder);

        Query query =
                NativeQuery.builder()
                        .withQuery(
                                QueryBuilders.multiMatch(
                                        builder ->
                                                builder.query(searchKeyword)
                                                        .fields(fieldNames)
                                                        .operator(Operator.Or)))
                        .withSort(Sort.by(direction, sortFields))
                        .withAggregation("MyBorough", boroughTermsBuilder)
                        .withAggregation("MyCuisine", cuisineTermsBuilder)
                        .withAggregation("MyDateRange", dateRangeBuilder)
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
}
