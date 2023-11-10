package com.example.graphql.querydsl.model.query;

public record FindPostCommentsQuery(int pageNo, int pageSize, String sortBy, String sortDir) {}
