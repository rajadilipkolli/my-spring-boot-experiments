package com.example.jndi.model.query;

public record FindDriversQuery(int pageNo, int pageSize, String sortBy, String sortDir) {}
