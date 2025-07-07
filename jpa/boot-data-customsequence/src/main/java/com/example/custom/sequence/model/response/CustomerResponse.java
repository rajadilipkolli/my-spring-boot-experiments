package com.example.custom.sequence.model.response;

import java.util.List;

/**
 * Response DTO representing customer information with their associated orders.
 *
 * @param id Customer's unique identifier
 * @param text Customer's descriptive text
 * @param orderResponses List of associated orders, never null but may be empty
 */
public record CustomerResponse(String id, String text, List<OrderResponseWithOutCustomer> orderResponses) {
    public CustomerResponse {
        orderResponses = orderResponses == null ? List.of() : orderResponses;
    }
}
