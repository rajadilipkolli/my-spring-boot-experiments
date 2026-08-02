package com.example.highrps.author.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthorRequest(
        @NotBlank(message = "FirstName must not be blank") String firstName,
        String middleName,
        @NotBlank(message = "LastName Can't be Blank") String lastName,

        @Positive(message = "Mobile Number should be positive")
        Long mobile,

        @Email @NotBlank(message = "Email Can't be Blank") String email,
        LocalDateTime registeredAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt)
        implements Serializable {}
