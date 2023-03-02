package com.example.jooq.r2dbc.model.response;

import java.util.List;

public record PaginatedResult(List<?> data, Long count) {}
