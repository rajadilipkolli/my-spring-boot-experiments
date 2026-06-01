package com.example.graphql.dtos;

import java.util.List;

public class CustomerConnection {
    private final List<CustomerEdge> edges;
    private final PageInfo pageInfo;

    public CustomerConnection(List<CustomerEdge> edges, PageInfo pageInfo) {
        this.edges = edges;
        this.pageInfo = pageInfo;
    }

    public List<CustomerEdge> getEdges() {
        return edges;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }
}
