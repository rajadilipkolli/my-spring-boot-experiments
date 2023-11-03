package com.example.envers.model.query;

public record FindCustomersQuery(int pageNo, int pageSize, String sortBy, String sortDir) {}
