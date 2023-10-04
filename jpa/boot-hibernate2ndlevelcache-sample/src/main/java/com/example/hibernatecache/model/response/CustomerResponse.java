package com.example.hibernatecache.model.response;

import java.util.List;

public record CustomerResponse(
        Long customerId,
        String firstName,
        String lastName,
        String email,
        String phone,
        List<OrderResponse> orders) {}
