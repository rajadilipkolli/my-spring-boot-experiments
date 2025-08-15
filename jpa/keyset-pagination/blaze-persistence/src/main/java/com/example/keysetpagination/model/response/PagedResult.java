package com.example.keysetpagination.model.response;

import com.blazebit.persistence.spring.data.repository.KeysetAwarePage;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PagedResult<T>(
        List<T> data,
        long totalElements,
        int pageNumber,
        int totalPages,
        @JsonProperty("isFirst") boolean isFirst,
        @JsonProperty("isLast") boolean isLast,
        @JsonProperty("hasNext") boolean hasNext,
        @JsonProperty("hasPrevious") boolean hasPrevious,
        KeySetPageResponse keySetPageResponse) {
    public PagedResult(KeysetAwarePage<T> page) {
        this(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious(),
                new KeySetPageResponse(
                        page.getKeysetPage().getMaxResults(),
                        page.getKeysetPage().getFirstResult(),
                        (Long) page.getKeysetPage().getLowest().getTuple()[0],
                        (Long) page.getKeysetPage().getHighest().getTuple()[0]));
    }

    public <R> PagedResult<R> toResponseRecord(List<R> data) {
        return new PagedResult<>(
                data, totalElements, pageNumber, totalPages, isFirst, isLast, hasNext, hasPrevious, keySetPageResponse);
    }
}
