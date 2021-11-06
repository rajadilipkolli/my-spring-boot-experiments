package com.example.graphql.querydsl.model.response;

import com.example.graphql.querydsl.model.PostCommentsDTO;
import com.example.graphql.querydsl.model.TagDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
                           String title,
                           String content,
                           String createdBy,
                           LocalDateTime createdOn,
                           @JsonProperty("comments") List<PostCommentsDTO> comments,
                           @JsonProperty("tags") List<TagDTO> tags) {
}
