package com.example.mongoes.response;

import com.example.mongoes.document.Restaurant;
import java.util.List;
import java.util.Map;
import org.springframework.data.elasticsearch.core.SearchPage;

public record AggregationSearchResponse(
        SearchPage<Restaurant> searchResults,
        List<Map<String, Map<String, Object>>> facets,
        long totalHits) {}
