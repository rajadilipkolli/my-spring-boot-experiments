package com.example.custom.sequence.model.response;

import java.util.List;

public record CustomerResponse(
        String id, String text, List<OrderResponseWithOutCustomer> orderResponses) {}
