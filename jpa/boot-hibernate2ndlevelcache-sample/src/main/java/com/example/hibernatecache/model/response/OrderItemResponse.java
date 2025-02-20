package com.example.hibernatecache.model.response;

import java.math.BigDecimal;

public record OrderItemResponse(Long orderItemId, String itemCode, BigDecimal price, Integer quantity) {}
