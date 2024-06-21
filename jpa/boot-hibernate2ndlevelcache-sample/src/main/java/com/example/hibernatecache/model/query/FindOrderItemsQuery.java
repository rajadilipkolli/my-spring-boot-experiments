package com.example.hibernatecache.model.query;

public record FindOrderItemsQuery(int pageNo, int pageSize, String sortBy, String sortDir) {}
