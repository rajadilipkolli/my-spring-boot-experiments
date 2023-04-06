package com.example.mongoes.response;

import com.example.mongoes.document.Restaurant;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;

import java.util.List;
import java.util.Map;

public record AggregationSearchResponse(
        List<SearchHit<Restaurant>> content,
        Map<String, Map<String, Long>> facets,
        Pageable pageable,
        int totalPages,
        long numberOfElements) {}
