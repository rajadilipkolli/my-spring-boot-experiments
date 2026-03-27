package com.example.highrps.author;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;
import java.time.LocalDateTime;

public record AuthorRequest(
        @NotBlank(message = "FirstName Can't be Blank") String firstName,
        String middleName,
        @NotBlank(message = "LastName Can't be Blank") String lastName,

        @Positive(message = "Mobile Number should be positive")
        Long mobile,

        @Email @NotBlank(message = "Email Can't be Blank") String email,
        LocalDateTime registeredAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt)
        implements Serializable {

    public AuthorRequest withTimestamps(LocalDateTime registeredAt, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        return new AuthorRequest(
                this.firstName,
                this.middleName,
                this.lastName,
                this.mobile,
                this.email,
                registeredAt,
                createdAt,
                modifiedAt);
    }
}
