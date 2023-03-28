package com.example.jooq.r2dbc.model.request;

import java.util.List;
import java.util.UUID;

public record CreatePostCommand(String title, String content, List<UUID> tagId) {}
