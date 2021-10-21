package com.example.graphql.querydsl.model.response;

import com.example.graphql.querydsl.model.PostCommentsDTO;
import com.example.graphql.querydsl.model.TagDTO;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
                           String title,
                           String content,
                           String createdBy,
                           LocalDateTime createdOn,
                           List<PostCommentsDTO> comments,
                           List<TagDTO> tags) {
}
