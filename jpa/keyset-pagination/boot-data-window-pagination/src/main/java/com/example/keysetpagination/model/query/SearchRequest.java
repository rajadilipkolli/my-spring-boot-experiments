package com.example.keysetpagination.model.query;

import java.util.List;

public class SearchRequest {

    private List<SearchCriteria> searchCriteriaList;

    private List<SortRequest> sortRequests;

    /**
 * Retrieves the list of search criteria.
 *
 * @return A List of SearchCriteria objects representing the current search criteria
 */
public List<SearchCriteria> getSearchCriteriaList() {
    return searchCriteriaList;
}

    /**
 * Sets the list of search criteria for filtering operations.
 *
 * @param searchCriteriaList the list of search criteria to be applied; can be null to clear existing criteria
 */
public void setSearchCriteriaList(List<SearchCriteria> searchCriteriaList) {
    this.searchCriteriaList = searchCriteriaList;
}

    /**
 * Retrieves the list of sort requests.
 *
 * @return A List of SortRequest objects representing the current sort criteria
 */
public List<SortRequest> getSortDtos() {
    return sortRequests;
}

    /**
 * Sets the list of sort requests for ordering results.
 *
 * @param sortRequests the list of sort criteria to be applied; can be null to clear existing sort requests
 */
public void setSortDtos(List<SortRequest> sortRequests) {
    this.sortRequests = sortRequests;
}
}
