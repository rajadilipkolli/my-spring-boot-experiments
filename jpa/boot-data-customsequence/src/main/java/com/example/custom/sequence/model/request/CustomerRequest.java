package com.example.custom.sequence.model.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CustomerRequest(
        @NotBlank(message = "Text cannot be empty") String text, List<OrderRequest> orders) {}
