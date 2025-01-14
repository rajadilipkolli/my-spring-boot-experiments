package com.example.learning.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TagRequest(
        @NotBlank(message = "Name of tag is mandatory") @Size(min = 1, max = 50, message = "Tag name must be between 1 and 50 characters") @Pattern(
                        regexp = "^[a-zA-Z0-9-_]+$",
                        message = "Tag name can only contain letters, numbers, hyphens and underscores")
                String name,
        @Size(max = 200, message = "Tag description cannot exceed 200 characters") String description) {}
