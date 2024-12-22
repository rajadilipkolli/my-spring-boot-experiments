package com.example.custom.sequence.model.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CustomerRequest(
        @NotEmpty(message = "Text cannot be empty") String text, List<OrderRequest> orders) {}
