package com.example.mongoes.model.response;

import com.example.mongoes.document.Restaurant;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

public record AggregationSearchResponse(
        List<Restaurant> content,
        Map<String, Map<String, Long>> facets,
        Pageable pageable,
        int totalPages,
        long numberOfElements) {}
