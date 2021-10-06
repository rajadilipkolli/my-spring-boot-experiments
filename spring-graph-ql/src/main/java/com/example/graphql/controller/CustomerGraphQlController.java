package com.example.graphql.controller;

import com.example.graphql.record.Customer;
import com.example.graphql.record.Order;
import com.example.graphql.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Random;

@Controller
@RequiredArgsConstructor
public class CustomerGraphQlController {

    private final CustomerRepository customerRepository;

//    @SchemaMapping(typeName = "Query", field = "customers") or
    @QueryMapping
    Flux<Customer> customers() {
        return this.customerRepository.findAll();
    }

    @SchemaMapping(typeName = "Customer")
    Flux<Order> orders(Customer customer) {
        // Could be webservice call
        var orders = new ArrayList<Order>();
        for (var orderId = 1; orderId <= Math.random() *100; orderId++) {
            orders.add(new Order(orderId, customer.id()));
        }
        return Flux.fromIterable(orders);
    }
}
