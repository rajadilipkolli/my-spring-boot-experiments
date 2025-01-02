package com.example.custom.sequence.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CustomerRequest(
        @NotBlank(message = "Text cannot be empty") String text,
        @Valid List<OrderRequest> orders) {}
