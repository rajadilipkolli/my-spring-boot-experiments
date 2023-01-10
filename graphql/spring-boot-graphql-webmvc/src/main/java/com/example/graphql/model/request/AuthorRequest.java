package com.example.graphql.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AuthorRequest(
        @NotBlank(message = "FirstName Cant be Blank") String firstName,
        String middleName,
        @NotBlank(message = "LastName Cant be Blank") String lastName,
        String mobile,
        @Email @NotBlank(message = "Email Cant be Blank") String email) {}
