package com.example.custom.sequence.mapper;

import com.example.custom.sequence.entities.Customer;
import com.example.custom.sequence.entities.Order;
import com.example.custom.sequence.model.request.OrderRequest;
import com.example.custom.sequence.model.response.CustomerResponseWithOutOrder;
import com.example.custom.sequence.model.response.OrderResponse;
import com.example.custom.sequence.model.response.OrderResponseWithOutCustomer;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderMapper {

    public OrderResponse getOrderResponse(Order order) {
        Customer customer = order.getCustomer();
        return new OrderResponse(
                order.getId(),
                order.getItemName(),
                new CustomerResponseWithOutOrder(customer.getId(), customer.getName()));
    }

    public Order mapToEntity(OrderRequest orderRequest) {
        Order order = new Order();
        order.setItemName(orderRequest.itemName());
        return order;
    }

    public List<OrderResponse> mapToResponseList(List<Order> orders) {
        return orders.stream().map(this::getOrderResponse).toList();
    }

    public List<OrderResponseWithOutCustomer> mapToResponseListWithOutCustomer(List<Order> orders) {
        return orders.stream()
                .map(order -> new OrderResponseWithOutCustomer(order.getId(), order.getItemName()))
                .toList();
    }

    public Order mapToEntityWithCustomer(OrderRequest orderRequest, Customer foundCustomer) {
        Order order = mapToEntity(orderRequest);
        order.setCustomer(foundCustomer);
        return order;
    }
}
