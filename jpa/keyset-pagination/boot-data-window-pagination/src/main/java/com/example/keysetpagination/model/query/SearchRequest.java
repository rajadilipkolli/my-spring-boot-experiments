package com.example.keysetpagination.model.query;

import java.util.ArrayList;
import java.util.List;

public class SearchRequest {

    private List<SearchCriteria> searchCriteriaList;

    private List<SortRequest> sortRequests;

    public SearchRequest() {
        this.searchCriteriaList = new ArrayList<>();
        this.sortRequests = new ArrayList<>();
    }

    public SearchRequest(List<SearchCriteria> searchCriteriaList, List<SortRequest> sortRequests) {
        this.searchCriteriaList = searchCriteriaList != null ? searchCriteriaList : new ArrayList<>();
        this.sortRequests = sortRequests != null ? sortRequests : new ArrayList<>();
    }

    public List<SearchCriteria> getSearchCriteriaList() {
        return searchCriteriaList;
    }

    public SearchRequest setSearchCriteriaList(List<SearchCriteria> searchCriteriaList) {
        this.searchCriteriaList = searchCriteriaList;
        return this;
    }

    public List<SortRequest> getSortRequests() {
        return sortRequests;
    }

    public SearchRequest setSortRequests(List<SortRequest> sortRequests) {
        this.sortRequests = sortRequests;
        return this;
    }

    @Override
    public String toString() {
        return "SearchRequest{" + "searchCriteria=" + searchCriteriaList + ", sortRequests=" + sortRequests + '}';
    }
}
