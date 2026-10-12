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
    /**
     * Creates a customer response, replacing a null order list with an empty list.
     *
     * @param id the customer identifier
     * @param customerName the customer name
     * @param orderResponses the order summaries, or null when none are supplied
     */
    public CustomerResponse {
        orderResponses = orderResponses == null ? List.of() : orderResponses;
    }
}
