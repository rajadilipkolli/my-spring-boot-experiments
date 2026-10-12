package com.example.custom.sequence.model.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Order details accepted by create and update operations.
 *
 * @param itemName the required, nonblank item name
 * @param customerId the owning customer identifier, required when the group check is applied
 */
public record OrderRequest(
        @NotBlank(message = "Item name cannot be blank") String itemName,

        @NotBlank(message = "CustomerId cannot be blank", groups = ValidationGroups.GroupCheck.class) String customerId) {}
