package com.example.hibernatecache.model.query;

public record FindCustomersQuery(int pageNo, int pageSize, String sortBy, String sortDir) {}
