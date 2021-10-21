package com.example.graphql.querydsl.model.request;

import com.example.graphql.querydsl.model.PostCommentsDTO;
import com.example.graphql.querydsl.model.TagDTO;

import javax.validation.constraints.NotBlank;
import java.util.List;

public record PostRequestDTO(@NotBlank String name,
                             @NotBlank String title,
                             @NotBlank String content,
                             List<PostCommentsDTO> comments,
                             List<TagDTO> tags) {
}
