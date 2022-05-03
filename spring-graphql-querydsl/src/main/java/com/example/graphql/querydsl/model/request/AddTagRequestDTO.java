package com.example.graphql.querydsl.model.request;

import com.example.graphql.querydsl.model.TagDTO;
import java.util.List;

public record AddTagRequestDTO(List<TagDTO> tagNames, Long postId) {}
