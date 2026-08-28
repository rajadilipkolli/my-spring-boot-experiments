package com.example.graphql.model.query;

public record FindQuery(int pageNo, int pageSize, String sortBy, String sortDir) {}
