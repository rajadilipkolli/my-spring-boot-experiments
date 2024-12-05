package com.example.mongoes.web.service;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.RangeBucket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.stereotype.Service;

@Service
class AggregationProcessor {

    public Map<String, Map<String, Long>> processAggregations(
            Map<String, ElasticsearchAggregation> aggregationMap) {
        Map<String, Map<String, Long>> resultMap = new HashMap<>();
        aggregationMap.forEach(
                (String aggregateKey, ElasticsearchAggregation aggregation) -> {
                    Map<String, Long> countMap = new HashMap<>();
                    Aggregate aggregate = aggregation.aggregation().getAggregate();
                    if (aggregate.isSterms()) {
                        aggregate
                                .sterms()
                                .buckets()
                                .array()
                                .forEach(
                                        stringTermsBucket ->
                                                countMap.put(
                                                        stringTermsBucket.key().stringValue(),
                                                        stringTermsBucket.docCount()));
                    } else if (aggregate.isDateRange()) {
                        List<RangeBucket> bucketList = aggregate.dateRange().buckets().array();
                        bucketList.forEach(
                                rangeBucket -> {
                                    if (rangeBucket.docCount() != 0) {
                                        countMap.put(
                                                rangeBucket.fromAsString()
                                                        + " - "
                                                        + rangeBucket.toAsString(),
                                                rangeBucket.docCount());
                                    }
                                });
                    }
                    resultMap.put(aggregateKey, countMap);
                });

        return resultMap;
    }
}
