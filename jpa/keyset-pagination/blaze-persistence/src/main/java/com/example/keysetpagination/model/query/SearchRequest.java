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

    public SearchRequest() {}

    public List<ISearchCriteria<?>> getSearchCriteriaList() {
        return searchCriteriaList;
    }

    public void setSearchCriteriaList(List<ISearchCriteria<?>> searchCriteriaList) {
        this.searchCriteriaList = searchCriteriaList != null ? searchCriteriaList : new ArrayList<>();
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Long getLowest() {
        return lowest;
    }

    public void setLowest(Long lowest) {
        this.lowest = lowest;
    }

    public Long getHighest() {
        return highest;
    }

    public void setHighest(Long highest) {
        this.highest = highest;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }
}
