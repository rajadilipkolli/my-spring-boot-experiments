package com.example.custom.sequence.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CustomerRequest(
        @NotBlank(message = "Customer name cannot be blank") String customerName, List<@Valid OrderRequest> orders) {}
