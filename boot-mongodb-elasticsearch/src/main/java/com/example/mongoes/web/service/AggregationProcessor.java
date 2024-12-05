package com.example.mongoes.web.service;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.stereotype.Service;

/**
 * Processes Elasticsearch aggregations and transforms them into a structured map format. Supports
 * 'terms' and 'dateRange' aggregation types.
 */
@Service
class AggregationProcessor {

    /**
     * Processes Elasticsearch aggregations and returns a structured map of results.
     *
     * @param aggregationMap Map of aggregation key to ElasticsearchAggregation
     * @return Map of aggregation key to counts, where counts is a map of bucket key to document
     *     count
     * @throws IllegalArgumentException if aggregationMap is null
     */
    public Map<String, Map<String, Long>> processAggregations(
            Map<String, ElasticsearchAggregation> aggregationMap) {
        if (aggregationMap == null) {
            throw new IllegalArgumentException("aggregationMap must not be null");
        }
        Map<String, Map<String, Long>> resultMap = new HashMap<>();
        aggregationMap.forEach(
                (String aggregateKey, ElasticsearchAggregation aggregation) -> {
                    Map<String, Long> countMap = new HashMap<>();
                    Aggregate aggregate = aggregation.aggregation().getAggregate();
                    processAggregate(aggregate, countMap);
                    resultMap.put(aggregateKey, countMap);
                });
        return resultMap;
    }

    private void processAggregate(Aggregate aggregate, Map<String, Long> countMap) {
        if (aggregate.isSterms()) {
            processTermsAggregate(aggregate, countMap);
        } else if (aggregate.isDateRange()) {
            processDateRangeAggregate(aggregate, countMap);
        }
    }

    private void processTermsAggregate(Aggregate aggregate, Map<String, Long> countMap) {
        aggregate
                .sterms()
                .buckets()
                .array()
                .forEach(bucket -> countMap.put(bucket.key().stringValue(), bucket.docCount()));
    }

    private void processDateRangeAggregate(Aggregate aggregate, Map<String, Long> countMap) {
        aggregate.dateRange().buckets().array().stream()
                .filter(bucket -> bucket.docCount() != 0)
                .forEach(
                        bucket ->
                                countMap.put(
                                        bucket.fromAsString() + " - " + bucket.toAsString(),
                                        bucket.docCount()));
    }
}
