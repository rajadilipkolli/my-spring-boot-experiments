package com.example.custom.sequence.model.response;

/**
 * Order details returned by the API with a summary of the owning customer.
 *
 * @param id the generated order identifier
 * @param itemName the name of the ordered item
 * @param customer the customer summary without nested orders
 */
public record OrderResponse(String id, String itemName, CustomerResponseWithOutOrder customer) {}
