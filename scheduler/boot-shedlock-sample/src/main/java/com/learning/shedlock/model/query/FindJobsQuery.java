package com.learning.shedlock.model.query;

public record FindJobsQuery(int pageNo, int pageSize, String sortBy, String sortDir) {}
