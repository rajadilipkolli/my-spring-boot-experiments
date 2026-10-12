package com.example.custom.sequence.mapper;

import com.example.custom.sequence.entities.Customer;
import com.example.custom.sequence.entities.Order;
import com.example.custom.sequence.model.request.CustomerRequest;
import com.example.custom.sequence.model.response.CustomerResponse;
import com.example.custom.sequence.repositories.OrderRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CustomerMapper {

    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;

    public CustomerMapper(OrderMapper orderMapper, OrderRepository orderRepository) {
        this.orderMapper = orderMapper;
        this.orderRepository = orderRepository;
    }

    /**
     * Maps a customer and its orders to a response without nested customer references.
     *
     * @param saved the customer to expose
     * @return the customer response with its order summaries
     */
    public CustomerResponse mapToResponse(Customer saved) {
        return new CustomerResponse(
                saved.getId(), saved.getName(), orderMapper.mapToResponseListWithOutCustomer(saved.getOrders()));
    }

    /**
     * Creates a customer and associates any orders supplied in the request.
     *
     * @param customerRequest customer details; a null order list leaves the customer without orders
     * @return the new customer with both sides of each order association set
     */
    public Customer mapToEntity(CustomerRequest customerRequest) {
        Customer customer = new Customer().setName(customerRequest.customerName());
        if (customerRequest.orders() == null) {
            return customer;
        }
        customerRequest.orders().forEach(orderRequest -> customer.addOrder(orderMapper.mapToEntity(orderRequest)));
        return customer;
    }

    /**
     * Updates the customer name and reconciles supplied orders by item name.
     *
     * <p>A null order list preserves existing orders. Otherwise, omitted orders are removed, matching
     * orders retain their identifiers, and new orders are associated with the customer.
     *
     * @param customerRequest the requested name and optional replacement order list
     * @param foundCustomer the existing customer to update
     */
    public void updateCustomerFromRequest(CustomerRequest customerRequest, Customer foundCustomer) {
        foundCustomer.setName(customerRequest.customerName());
        if (customerRequest.orders() == null) {
            return;
        }
        List<Order> removedOrders = new ArrayList<>(foundCustomer.getOrders());
        List<Order> ordersFromRequest = customerRequest.orders().stream()
                .map(orderRequest -> orderMapper.mapToEntityWithCustomer(orderRequest, foundCustomer))
                .collect(Collectors.toList());
        removedOrders.removeAll(ordersFromRequest);

        for (Order removedOrder : removedOrders) {
            foundCustomer.removeOrder(removedOrder);
        }

        List<Order> newOrders = new ArrayList<>(ordersFromRequest);
        newOrders.removeAll(foundCustomer.getOrders());

        ordersFromRequest.removeAll(newOrders);

        for (Order existingOrder : ordersFromRequest) {
            existingOrder.setCustomer(foundCustomer);
            // manually set the id of the existing order to avoid creating a new order instead of
            // updating the existing one
            for (Order foundOrder : foundCustomer.getOrders()) {
                if (foundOrder.getItemName().equals(existingOrder.getItemName())) {
                    existingOrder.setId(foundOrder.getId());
                    break;
                }
            }
            Order mergedOrder = orderRepository.merge(existingOrder);
            foundCustomer.getOrders().set(foundCustomer.getOrders().indexOf(mergedOrder), mergedOrder);
        }

        for (Order newOrder : newOrders) {
            foundCustomer.addOrder(newOrder);
        }
    }
}
