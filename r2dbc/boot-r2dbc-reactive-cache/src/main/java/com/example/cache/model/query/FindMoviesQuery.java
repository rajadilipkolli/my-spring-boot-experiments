package com.example.cache.model.query;

public record FindMoviesQuery(int pageNo, int pageSize, String sortBy, String sortDir) {}
