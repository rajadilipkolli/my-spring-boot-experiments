package com.example.custom.sequence.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Customer details accepted by create and update operations.
 *
 * @param customerName the required, nonblank customer name
 * @param orders orders to create or reconcile; null leaves existing orders unchanged on update
 */
public record CustomerRequest(
        @NotBlank(message = "Customer name cannot be blank") String customerName, List<@Valid OrderRequest> orders) {}
