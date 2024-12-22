package com.example.custom.sequence.model.response;

/**
 * Response model representing an order without its associated customer details. Used in scenarios
 * where customer information is not needed in the response.
 *
 * @param id The unique identifier of the order
 * @param orderDescription The description or details of the order
 */
public record OrderResponseWithOutCustomer(String id, String orderDescription) {}
