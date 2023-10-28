package com.example.keysetpagination.model.query;

public record FindActorsQuery(
        int pageNo,
        int pageSize,
        Integer firstResult,
        Integer maxResults,
        Long lowest,
        Long highest,
        String sortBy,
        String sortDir) {}
