package com.example.custom.sequence.model.response;

import java.util.List;

/**
 * Response DTO representing customer information with their associated orders.
 *
 * @param id Customer's unique identifier
 * @param customerName Customer's descriptive name
 * @param orderResponses List of associated orders, never null but may be empty
 */
public record CustomerResponse(String id, String customerName, List<OrderResponseWithOutCustomer> orderResponses) {
    public CustomerResponse {
        orderResponses = orderResponses == null ? List.of() : orderResponses;
    }
}
