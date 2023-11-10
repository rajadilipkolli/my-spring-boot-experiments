package com.example.archunit.model.query;

public record FindClientsQuery(int pageNo, int pageSize, String sortBy, String sortDir) {}
