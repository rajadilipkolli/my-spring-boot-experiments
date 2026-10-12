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

    /**
     * Maps an order to a response with a summary of its customer.
     *
     * @param order the order with an associated customer
     * @return the order details and customer summary
     */
    public OrderResponse getOrderResponse(Order order) {
        Customer customer = order.getCustomer();
        return new OrderResponse(
                order.getId(),
                order.getItemName(),
                new CustomerResponseWithOutOrder(customer.getId(), customer.getName()));
    }

    /**
     * Creates an order containing the requested item name, without assigning a customer.
     *
     * @param orderRequest the requested order details
     * @return a new order whose customer must be assigned before persistence
     */
    public Order mapToEntity(OrderRequest orderRequest) {
        Order order = new Order();
        order.setItemName(orderRequest.itemName());
        return order;
    }

    public List<OrderResponse> mapToResponseList(List<Order> orders) {
        return orders.stream().map(this::getOrderResponse).toList();
    }

    /**
     * Maps orders to summaries that omit customer references.
     *
     * @param orders the orders to map in encounter order
     * @return the order summaries
     */
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
