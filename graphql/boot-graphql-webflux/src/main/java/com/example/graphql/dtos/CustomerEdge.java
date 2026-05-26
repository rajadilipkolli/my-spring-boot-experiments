package com.example.graphql.dtos;

public class CustomerEdge {
    private final Customer node;
    private final String cursor;

    public CustomerEdge(Customer node, String cursor) {
        this.node = node;
        this.cursor = cursor;
    }

    public Customer getNode() {
        return node;
    }

    public String getCursor() {
        return cursor;
    }
}
