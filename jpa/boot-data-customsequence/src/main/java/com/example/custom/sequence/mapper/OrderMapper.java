package com.example.custom.sequence.mapper;

import com.example.custom.sequence.entities.Customer;
import com.example.custom.sequence.entities.Order;
import com.example.custom.sequence.model.response.CustomerResponse;
import com.example.custom.sequence.model.response.OrderResponse;
import org.springframework.stereotype.Service;

@Service
public class OrderMapper {

    public OrderResponse getOrderResponse(Order order) {
        Customer customer = order.getCustomer();
        return new OrderResponse(
                order.getId(),
                order.getText(),
                new CustomerResponse(customer.getId(), customer.getText()));
    }
}
