package com.example.jooq.r2dbc.model.request;

import java.util.List;

public record CreatePostCommand(String title, String content, List<String> tagName) {}
