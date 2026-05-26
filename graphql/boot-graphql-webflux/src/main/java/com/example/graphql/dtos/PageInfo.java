package com.example.graphql.dtos;

public class PageInfo {
    private final boolean hasPreviousPage;
    private final boolean hasNextPage;
    private final String startCursor;
    private final String endCursor;

    public PageInfo(boolean hasPreviousPage, boolean hasNextPage, String startCursor, String endCursor) {
        this.hasPreviousPage = hasPreviousPage;
        this.hasNextPage = hasNextPage;
        this.startCursor = startCursor;
        this.endCursor = endCursor;
    }

    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public String getStartCursor() {
        return startCursor;
    }

    public String getEndCursor() {
        return endCursor;
    }
}
