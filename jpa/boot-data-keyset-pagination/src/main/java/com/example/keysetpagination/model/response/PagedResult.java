package com.example.keysetpagination.model.response;

import com.blazebit.persistence.PagedList;
import java.util.Arrays;
import java.util.List;

public record PagedResult<T>(
        List<T> content,
        int totalPages,
        int firstResult,
        int pageNo,
        int pageSize,
        long totalSize,
        int maxResults,
        KeySetPageResponse keySetPageResponse) {
    public PagedResult(PagedList<T> page) {
        this(
                page,
                page.getTotalPages(),
                page.getFirstResult(),
                page.getPage(),
                page.getSize(),
                page.getTotalSize(),
                page.getMaxResults(),
                new KeySetPageResponse(
                        page.getKeysetPage().getMaxResults(),
                        page.getKeysetPage().getFirstResult(),
                        Arrays.stream(page.getKeysetPage().getLowest().getTuple())
                                .map(String::valueOf)
                                .toList(),
                        Arrays.stream(page.getKeysetPage().getHighest().getTuple())
                                .map(String::valueOf)
                                .toList()));
    }
}
