package com.example.graphql.querydsl.model.request;

import com.example.graphql.querydsl.model.PostCommentsDTO;
import com.example.graphql.querydsl.model.TagDTO;
import java.util.List;
import javax.validation.constraints.NotBlank;

public record PostRequestDTO(
        @NotBlank String name,
        @NotBlank String title,
        @NotBlank String content,
        List<PostCommentsDTO> comments,
        List<TagDTO> tags) {}
