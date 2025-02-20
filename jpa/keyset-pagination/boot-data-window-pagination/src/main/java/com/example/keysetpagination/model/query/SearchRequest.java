package com.example.keysetpagination.model.query;

import java.util.ArrayList;
import java.util.List;

public class SearchRequest {

    private List<ISearchCriteria> searchCriteriaList;
    private List<SortRequest> sortRequests;
    private String scrollDirection;

    public SearchRequest() {
        this.searchCriteriaList = new ArrayList<>();
        this.sortRequests = new ArrayList<>();
        this.scrollDirection = "FORWARD";
    }

    public SearchRequest(
            List<ISearchCriteria> searchCriteriaList, List<SortRequest> sortRequests, String scrollDirection) {
        this.searchCriteriaList = searchCriteriaList != null ? searchCriteriaList : new ArrayList<>();
        this.sortRequests = sortRequests != null ? sortRequests : new ArrayList<>();
        this.scrollDirection = scrollDirection != null ? scrollDirection : "FORWARD";
    }

    public List<ISearchCriteria> getSearchCriteriaList() {
        return searchCriteriaList;
    }

    public SearchRequest setSearchCriteriaList(List<ISearchCriteria> searchCriteriaList) {
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

    public String getScrollDirection() {
        return scrollDirection;
    }

    public SearchRequest setScrollDirection(String scrollDirection) {
        this.scrollDirection = scrollDirection;
        return this;
    }

    @Override
    public String toString() {
        return "SearchRequest{" + "searchCriteriaList="
                + searchCriteriaList + ", sortRequests="
                + sortRequests + ", scrollDirection="
                + scrollDirection + "}";
    }
}
