package com.example.mongoes.repository.elasticsearch;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
import co.elastic.clients.elasticsearch._types.aggregations.DateRangeExpression;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.example.mongoes.document.Restaurant;
import java.util.ArrayList;
import java.util.List;
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

public class CustomRestaurantESRepositoryImpl implements CustomRestaurantESRepository {

    private static final String BOROUGH = "borough";
    private static final String CUISINE = "cuisine";
    private static final int PAGE_SIZE = 1_000;
    private final ReactiveElasticsearchOperations reactiveElasticsearchOperations;

    public CustomRestaurantESRepositoryImpl(
            ReactiveElasticsearchOperations reactiveElasticsearchOperations) {
        this.reactiveElasticsearchOperations = reactiveElasticsearchOperations;
    }

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
                                                rangeBuilder.term(
                                                        builder ->
                                                                builder.field("restaurant_id")
                                                                        .lte(
                                                                                String.valueOf(
                                                                                        upperLimit))
                                                                        .gte(
                                                                                String.valueOf(
                                                                                        lowerLimit)))))
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
                                                rangeBuilder.date(
                                                        builder ->
                                                                builder.field("grades.date")
                                                                        .lte(toDate)
                                                                        .gte(fromDate))))
                        .build();
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }

    /**
     * below is the console query
     *
     * <p>
     *
     * {@snippet :
     * """
     * POST /restaurant/_search?size=15&pretty
     * {
     * "query": {
     * "multi_match": {
     * "query": "Pizza",
     * "fields": [
     * "restaurant_name",
     * "borough",
     * "cuisine"
     * ],
     * "operator": "or"
     * }
     * },
     * "aggs": {
     * "MyCuisine": {
     * "terms": {
     * "field": "cuisine",
     * "size": 1000,
     * "order": {
     * "_count": "desc"
     * }
     * }
     * },
     * "MyBorough": {
     * "terms": {
     * "field": "borough",
     * "size": 1000
     * }
     * },
     * "MyDateRange": {
     * "date_range": {
     * "field": "grades.date",
     * "format": "dd-MM-yyy'T'hh:mm:ss",
     * "ranges": [
     * {
     * "key": "Older",
     * "to": "now-12y-1d/y"
     * },
     * {
     * "from": "now-12y/y",
     * "to": "now-11y/y"
     * },
     * {
     * "from": "now-11y/y",
     * "to": "now-10y/y"
     * },
     * {
     * "from": "now-10y/y",
     * "to": "now-9y/y"
     * },
     * {
     * "from": "now-9y/y",
     * "to": "now-8y/y"
     * },
     * {
     * "from": "now-8y/y",
     * "to": "now-7y/y"
     * },
     * {
     * "from": "now-7y/y",
     * "to": "now-6y/y"
     * },
     * {
     * "from": "now-6y/y",
     * "to": "now-5y/y"
     * },
     * {
     * "from": "now-5y/y",
     * "to": "now-4y/y"
     * },
     * {
     * "key": "Newer",
     * "from": "now-0y/y",
     * "to": "now/d"
     * }
     * ]
     * }
     * }
     * }
     * }
     * """;
     * }
     */
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
                                        .format("dd-MM-yyy")
                                        .timeZone("UTC")
                                        .ranges(getDateRanges()));

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

    private List<DateRangeExpression> getDateRanges() {
        List<DateRangeExpression> dateList = new ArrayList<>();

        // Create the "Older" DateRangeExpression
        dateList.add(
                DateRangeExpression.of(
                        builder ->
                                builder.key("Older").to(toBuilder -> toBuilder.expr("now-12y/y"))));

        // Create DateRangeExpressions for the middle years
        for (int i = 12; i > 0; i--) {
            dateList.add(createDateRangeExpression(i));
        }

        // Create the "Newer" DateRangeExpression
        dateList.add(
                DateRangeExpression.of(
                        builder ->
                                builder.key("Newer")
                                        .from(fromBuilder -> fromBuilder.expr("now-0y/y"))
                                        .to(toBuilder -> toBuilder.expr("now/d"))));

        return dateList;
    }

    private DateRangeExpression createDateRangeExpression(int yearsAgo) {
        return new DateRangeExpression.Builder()
                .from(fromBuilder -> fromBuilder.expr("now-%dy/y".formatted(yearsAgo)))
                .to(toBuilder -> toBuilder.expr("now-%dy/y".formatted(yearsAgo - 1)))
                .build();
    }

    @Override
    public Mono<SearchPage<Restaurant>> findAll(Pageable pageable) {
        Query query = new CriteriaQuery(Criteria.where("_id").exists());
        query.setPageable(pageable);

        return reactiveElasticsearchOperations.searchForPage(query, Restaurant.class);
    }
}
