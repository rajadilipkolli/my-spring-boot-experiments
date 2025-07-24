package com.example.opensearch.model.response;

import com.example.opensearch.entities.Restaurant;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.SearchPage;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PagedResult<T>(
        List<T> data,
        long totalElements,
        int pageNumber,
        int totalPages,
        Map<String, Map<String, Long>> aggregationMap,
        @JsonProperty("isFirst") boolean isFirst,
        @JsonProperty("isLast") boolean isLast,
        @JsonProperty("hasNext") boolean hasNext,
        @JsonProperty("hasPrevious") boolean hasPrevious) {

    public PagedResult(Page<T> page) {
        this(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getTotalPages(),
                new HashMap<>(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious());
    }

    public PagedResult(SearchPage<T> searchPage) {
        this(
                (List<T>) searchPage.getContent(),
                searchPage.getTotalElements(),
                searchPage.getNumber() + 1,
                searchPage.getTotalPages(),
                new HashMap<>(),
                searchPage.isFirst(),
                searchPage.isLast(),
                searchPage.hasNext(),
                searchPage.hasPrevious());
    }

    public PagedResult(SearchPage<Restaurant> searchPage, Map<String, Map<String, Long>> aggregationMap) {
        this(
                (List<T>) searchPage.getContent(),
                searchPage.getTotalElements(),
                searchPage.getNumber() + 1,
                searchPage.getTotalPages(),
                aggregationMap,
                searchPage.isFirst(),
                searchPage.isLast(),
                searchPage.hasNext(),
                searchPage.hasPrevious());
    }
}
