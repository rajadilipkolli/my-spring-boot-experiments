package com.example.jooq.r2dbc.model.response;

import java.util.List;
import java.util.UUID;

public record PostSummary(UUID id, String title, Long countOfComments, List<String> tags) {}
