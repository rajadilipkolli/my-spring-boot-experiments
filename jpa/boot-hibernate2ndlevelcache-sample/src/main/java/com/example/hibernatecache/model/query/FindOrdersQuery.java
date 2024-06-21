package com.example.hibernatecache.model.query;

public record FindOrdersQuery(int pageNo, int pageSize, String sortBy, String sortDir) {}
