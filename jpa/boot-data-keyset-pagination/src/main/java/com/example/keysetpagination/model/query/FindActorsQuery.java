package com.example.keysetpagination.model.query;

public record FindActorsQuery(int pageNo, int pageSize, String sortBy, String sortDir) {}
