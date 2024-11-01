package com.example.keysetpagination.model.query;

public record FindAnimalsQuery(int pageNo, int pageSize, String sortBy, String sortDir) {}
