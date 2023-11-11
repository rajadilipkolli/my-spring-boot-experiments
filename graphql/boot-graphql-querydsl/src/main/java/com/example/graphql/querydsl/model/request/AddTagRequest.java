package com.example.graphql.querydsl.model.request;

import java.util.List;

public record AddTagRequest(List<TagRequest> tagNames, Long postId) {}
