package com.example.opensearch.repositories;

import com.example.opensearch.entities.Restaurant;
import com.example.opensearch.model.response.PagedResult;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opensearch.data.client.orhlc.NativeSearchQuery;
import org.opensearch.data.client.orhlc.NativeSearchQueryBuilder;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.MultiMatchQueryBuilder;
import org.opensearch.index.query.Operator;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.BucketOrder;
import org.opensearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.GeoDistanceOrder;
import org.springframework.data.elasticsearch.core.query.Query;

public class CustomRestaurantRepositoryImpl implements CustomRestaurantRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    private static final int PAGE_SIZE = 1_000;

    public CustomRestaurantRepositoryImpl(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public SearchHitsIterator<Restaurant> searchWithin(GeoPoint geoPoint, Double distance, String unit) {

        Query query = new CriteriaQuery(new Criteria("address.coord").within(geoPoint, distance.toString() + unit));

        // add a sort to get the actual distance back in the sort value
        Sort sort = Sort.by(new GeoDistanceOrder("address.coord", geoPoint).withUnit(unit));
        query.addSort(sort);

        return elasticsearchOperations.searchForStream(query, Restaurant.class);
    }

    @Override
    public PagedResult<Restaurant> findByBoroughOrCuisineOrName(
            String queryKeyWord, Boolean prefixPhraseEnabled, Pageable pageable) {
        MultiMatchQueryBuilder multiMatchQuery =
                QueryBuilders.multiMatchQuery(queryKeyWord, "borough", "cuisine", "name");
        if (prefixPhraseEnabled) {
            multiMatchQuery.type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX);
        }

        Query query = new NativeSearchQuery(multiMatchQuery);
        query.setPageable(pageable);

        return getResults(query);
    }

    private PagedResult<Restaurant> getResults(Query query) {
        SearchHits<Restaurant> searchHits = elasticsearchOperations.search(query, Restaurant.class);
        SearchPage<Restaurant> searchPage = SearchHitSupport.searchPageFor(searchHits, query.getPageable());
        return new PagedResult<>(searchPage);
    }

    @Override
    public PagedResult<Restaurant> termQueryForBorough(String queryTerm, Pageable pageable) {
        Query query = new NativeSearchQuery(
                QueryBuilders.termQuery("borough", queryTerm).caseInsensitive(true));
        query.setPageable(pageable);

        return getResults(query);
    }

    @Override
    public PagedResult<Restaurant> termsQueryForBorough(List<String> queries, Pageable pageable) {
        Query query = new NativeSearchQuery(QueryBuilders.termsQuery(
                "borough", queries.stream().map(String::toLowerCase).toList()));
        query.setPageable(pageable);

        return getResults(query);
    }

    @Override
    public PagedResult<Restaurant> queryBoolWithShould(String borough, String cuisine, String name, Pageable pageable) {
        BoolQueryBuilder queryBuilders = new BoolQueryBuilder();
        queryBuilders.should(QueryBuilders.matchQuery("borough", borough));
        queryBuilders.should(QueryBuilders.wildcardQuery("cuisine", "*" + cuisine + "*"));
        queryBuilders.should(QueryBuilders.matchQuery("name", name));
        Query query = new NativeSearchQuery(queryBuilders);
        query.setPageable(pageable);

        return getResults(query);
    }

    @Override
    public PagedResult<Restaurant> wildcardSearch(String queryKeyword, Pageable pageable) {
        BoolQueryBuilder queryBuilders = new BoolQueryBuilder();
        queryBuilders.should(QueryBuilders.wildcardQuery("borough", "*" + queryKeyword + "*"));
        queryBuilders.should(QueryBuilders.wildcardQuery("cuisine", "*" + queryKeyword + "*"));
        queryBuilders.should(QueryBuilders.wildcardQuery("name", "*" + queryKeyword + "*"));
        Query query = new NativeSearchQuery(queryBuilders);
        query.setPageable(pageable);

        return getResults(query);
    }

    @Override
    public PagedResult<Restaurant> regExpSearch(String reqEx, Pageable pageable) {
        Query query = new NativeSearchQuery(
                QueryBuilders.regexpQuery("borough", reqEx).caseInsensitive(true));
        query.setPageable(pageable);

        return getResults(query);
    }

    @Override
    public PagedResult<Restaurant> searchSimpleQueryForBoroughAndCuisine(String queryKeyword, Pageable pageable) {
        Map<String, Float> map = new HashMap<>();
        map.put("borough", 1.0F);
        map.put("cuisine", 2.0F);
        Query query = new NativeSearchQuery(
                QueryBuilders.simpleQueryStringQuery(queryKeyword).fields(map));
        query.setPageable(pageable);

        return getResults(query);
    }

    @Override
    public PagedResult<Restaurant> searchRestaurantIdRange(Long lowerLimit, Long upperLimit, Pageable pageable) {
        Query query = new NativeSearchQuery(
                QueryBuilders.rangeQuery("id").gte(lowerLimit).lte(upperLimit));
        query.setPageable(pageable);

        return getResults(query);
    }

    @Override
    public PagedResult<Restaurant> searchDateRange(String fromDate, String toDate, Pageable pageable) {
        Query query = new NativeSearchQuery(
                QueryBuilders.rangeQuery("grades.date").gte(fromDate).lte(toDate));
        query.setPageable(pageable);

        return getResults(query);
    }

    @Override
    public SearchPage<Restaurant> aggregateSearch(
            String searchKeyword,
            List<String> fieldNames,
            Sort.Direction direction,
            Integer limit,
            Integer offset,
            String[] sortFields) {
        TermsAggregationBuilder cuisineTermsBuilder = AggregationBuilders.terms("MyCuisine")
                .field("cuisine")
                .size(PAGE_SIZE)
                .order(BucketOrder.count(false));
        TermsAggregationBuilder boroughTermsBuilder = AggregationBuilders.terms("MyBorough")
                .field("borough")
                .size(PAGE_SIZE)
                .order(BucketOrder.count(false));
        DateRangeAggregationBuilder dateRangeBuilder =
                AggregationBuilders.dateRange("MyDateRange").field("grades.date");
        addDateRange(dateRangeBuilder);

        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(searchKeyword, fieldNames.toArray(String[]::new))
                        .operator(Operator.OR))
                .withSort(Sort.by(direction, sortFields))
                .withAggregations(cuisineTermsBuilder, boroughTermsBuilder, dateRangeBuilder)
                .build();
        query.setPageable(PageRequest.of(offset, limit));

        return SearchHitSupport.searchPageFor(
                elasticsearchOperations.search(query, Restaurant.class), query.getPageable());
    }

    private void addDateRange(DateRangeAggregationBuilder dateRangeBuilder) {
        ZonedDateTime zonedDateTime =
                ZonedDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay(ZoneId.of("UTC"));
        dateRangeBuilder.addUnboundedTo(zonedDateTime.minusMonths(12L));
        for (long i = 12; i > 0; i--) {
            dateRangeBuilder.addRange(
                    zonedDateTime.minusMonths(i),
                    zonedDateTime.minusMonths(i - 1L).minusSeconds(1L));
        }
        dateRangeBuilder.addUnboundedFrom(zonedDateTime);
    }
}
