package com.example.graphql.querydsl.model.query;

public record FindTagsQuery(int pageNo, int pageSize, String sortBy, String sortDir) {}
