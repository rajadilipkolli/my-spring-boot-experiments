package com.example.highrps.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;

public record AuthorRequest(
        @NotBlank(message = "FirstName Cant be Blank") String firstName,
        String middleName,
        @NotBlank(message = "LastName Cant be Blank") String lastName,

        @Positive(message = "Mobile Number should be positive") Long mobile,

        @Email @NotBlank(message = "Email Cant be Blank") String email)
        implements Serializable {}
