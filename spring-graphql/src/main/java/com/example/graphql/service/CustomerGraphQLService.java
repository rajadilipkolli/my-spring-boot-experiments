package com.example.graphql.service;

import com.example.graphql.dtos.Customer;
import com.example.graphql.dtos.Orders;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface CustomerGraphQLService {
    Flux<Customer> findAllCustomers();

    Flux<Customer> findByNameIgnoringCase(String name);

    Mono<Map<Customer, List<Orders>>> findAllOrdersByCustomers(List<Customer> customers);

    Mono<Customer> addCustomer(String name);

    Mono<Orders> addOrderToCustomer(Integer id);
}
