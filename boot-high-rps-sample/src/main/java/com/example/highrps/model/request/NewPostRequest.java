package com.example.highrps.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

public record NewPostRequest(
        @NotBlank(message = "PostTitle must not be blank") String title,
        @NotBlank(message = "PostContent must not be blank") String content,

        @NotBlank(message = "Email must not be blank") @Email(message = "Provide valid email") String email,

        Boolean published,
        @Valid PostDetailsRequest details,
        @Valid List<TagRequest> tags)
        implements Serializable {}
