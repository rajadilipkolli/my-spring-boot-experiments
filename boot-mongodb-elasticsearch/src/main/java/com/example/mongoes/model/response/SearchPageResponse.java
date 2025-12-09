package com.example.mongoes.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SearchPageResponse<T>(
        List<T> data,
        long totalHits,
        float maxScore,
        Map<String, Map<String, Long>> facets,
        long totalElements,
        int pageNumber,
        int totalPages,
        @JsonProperty("isFirst") boolean isFirst,
        @JsonProperty("isLast") boolean isLast,
        @JsonProperty("hasNext") boolean hasNext,
        @JsonProperty("hasPrevious") boolean hasPrevious) {
    public SearchPageResponse(SearchPage<T> searchHits) {
        this(
                searchHits.getContent().stream().map(SearchHit::getContent).toList(),
                searchHits.getSearchHits().getTotalHits(),
                searchHits.getSearchHits().getMaxScore(),
                null,
                searchHits.getTotalElements(),
                searchHits.getPageable().getPageNumber() + 1,
                searchHits.getTotalPages(),
                searchHits.isFirst(),
                searchHits.isLast(),
                searchHits.hasNext(),
                searchHits.hasPrevious());
    }

    public SearchPageResponse(SearchPage<T> searchPage, Map<String, Map<String, Long>> facets) {
        this(
                searchPage.getContent().stream().map(SearchHit::getContent).toList(),
                searchPage.getSearchHits().getTotalHits(),
                searchPage.getSearchHits().getMaxScore(),
                facets,
                searchPage.getTotalElements(),
                searchPage.getPageable().getPageNumber() + 1,
                searchPage.getTotalPages(),
                searchPage.isFirst(),
                searchPage.isLast(),
                searchPage.hasNext(),
                searchPage.hasPrevious());
    }
}
