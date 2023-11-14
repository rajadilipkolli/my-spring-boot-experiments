package com.example.graphql.querydsl.model.query;

public record FindQuery(int pageNo, int pageSize, String sortBy, String sortDir) {}
