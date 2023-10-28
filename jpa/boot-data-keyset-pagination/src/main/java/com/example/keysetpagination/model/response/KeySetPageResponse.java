package com.example.keysetpagination.model.response;

import java.util.List;

public record KeySetPageResponse(
        int maxResults, int firstResult, List<String> lowest, List<String> highest) {}
