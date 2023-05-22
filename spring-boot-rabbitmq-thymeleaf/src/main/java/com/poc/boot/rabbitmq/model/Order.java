package com.poc.boot.rabbitmq.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private String orderNumber;

    private String productId;

    private Double amount;
}
