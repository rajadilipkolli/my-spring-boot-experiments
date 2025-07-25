package com.example.keysetpagination.model.query;

public record FindActorsQuery(int pageNo, int pageSize, Long lowest, Long highest, String sortBy, String sortDir) {}
