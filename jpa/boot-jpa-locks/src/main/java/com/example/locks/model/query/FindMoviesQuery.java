package com.example.locks.model.query;

public record FindMoviesQuery(int pageNo, int pageSize, String sortBy, String sortDir) {}
