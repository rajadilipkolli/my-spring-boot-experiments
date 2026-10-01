package com.example.keysetpagination.model.query;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;

public class SearchRequest {

    @Valid private List<ISearchCriteria<?>> searchCriteriaList = new ArrayList<>();

    @Min(0) private int pageNo = 0;

    @Min(1) private int pageSize = 10;

    private Long lowest;
    private Long highest;
    private String sortBy = "id";
    private String sortDir = "asc";

    /** Creates a request with no filters, page number 0, page size 10, and ascending ID sorting. */
    public SearchRequest() {}

    /** Returns the top-level filters and groups, which are combined with AND. */
    public List<ISearchCriteria<?>> getSearchCriteriaList() {
        return searchCriteriaList;
    }

    /** Replaces the top-level filters and groups, normalizing null to an empty list. */
    public void setSearchCriteriaList(List<ISearchCriteria<?>> searchCriteriaList) {
        this.searchCriteriaList = searchCriteriaList != null ? searchCriteriaList : new ArrayList<>();
    }

    /** Returns the requested page number, defaulting to 0. */
    public int getPageNo() {
        return pageNo;
    }

    /** Sets the requested page number, which bean validation requires to be nonnegative. */
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    /** Returns the requested page size, defaulting to 10. */
    public int getPageSize() {
        return pageSize;
    }

    /** Sets the requested page size, which bean validation requires to be at least 1. */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /** Returns the optional lower keyset bound, or null when omitted. */
    public Long getLowest() {
        return lowest;
    }

    /** Sets the optional lower keyset bound; null leaves it unspecified. */
    public void setLowest(Long lowest) {
        this.lowest = lowest;
    }

    /** Returns the optional upper keyset bound, or null when omitted. */
    public Long getHighest() {
        return highest;
    }

    /** Sets the optional upper keyset bound; null leaves it unspecified. */
    public void setHighest(Long highest) {
        this.highest = highest;
    }

    /** Returns the sort property, defaulting to id. */
    public String getSortBy() {
        return sortBy;
    }

    /** Sets the entity property used for sorting. */
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    /** Returns the requested sort direction, defaulting to asc. */
    public String getSortDir() {
        return sortDir;
    }

    /** Sets the sort direction; the service treats asc case-insensitively as ascending. */
    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }
}
