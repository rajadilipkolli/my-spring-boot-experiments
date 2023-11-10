package com.example.graphql.querydsl.model.query;

public record FindPostsQuery(int pageNo, int pageSize, String sortBy, String sortDir) {}
