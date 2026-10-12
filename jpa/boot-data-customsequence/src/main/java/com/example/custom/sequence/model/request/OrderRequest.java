package com.example.custom.sequence.model.request;

import jakarta.validation.constraints.NotBlank;

public record OrderRequest(
        @NotBlank(message = "Item name cannot be blank") String itemName,

        @NotBlank(message = "CustomerId cannot be blank", groups = ValidationGroups.GroupCheck.class) String customerId) {}
