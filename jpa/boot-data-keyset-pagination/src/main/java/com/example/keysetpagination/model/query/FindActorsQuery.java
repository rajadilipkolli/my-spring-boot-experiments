package com.example.keysetpagination.model.query;

public record FindActorsQuery(
        int pageNo,
        int pageSize,
        int firstResult,
        int maxResults,
        Long lowest,
        Long highest,
        String sortBy,
        String sortDir) {
    public FindActorsQuery(int pageSize, String sortBy, String sortDir) {
        this(0, pageSize, 0, 0, null, null, sortBy, sortDir);
    }
}
