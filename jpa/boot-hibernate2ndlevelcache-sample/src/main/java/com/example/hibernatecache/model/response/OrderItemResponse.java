package com.example.hibernatecache.model.response;

import java.math.BigDecimal;

public record OrderItemResponse(Long orderItemId, BigDecimal price, Integer quantity) {}
