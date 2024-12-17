package com.example.keysetpagination.model.query;

import java.util.List;

public class SearchRequest {

    private List<SearchCriteria> searchCriteriaList;

    private List<SortRequest> sortRequests;

    public List<SearchCriteria> getSearchCriteriaList() {
        return searchCriteriaList;
    }

    public void setSearchCriteriaList(List<SearchCriteria> searchCriteriaList) {
        this.searchCriteriaList = searchCriteriaList;
    }

    public List<SortRequest> getSortDtos() {
        return sortRequests;
    }

    public void setSortDtos(List<SortRequest> sortRequests) {
        this.sortRequests = sortRequests;
    }
}
