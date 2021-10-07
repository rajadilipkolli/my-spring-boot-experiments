package com.example.graphql.controller;

import com.example.graphql.dtos.Customer;
import com.example.graphql.dtos.Order;
import com.example.graphql.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.util.ArrayList;

@Controller
@RequiredArgsConstructor
public class CustomerGraphQLController {

  private final CustomerRepository customerRepository;

  //    @SchemaMapping(typeName = "Query", field = "customers") or
  @QueryMapping
  Flux<Customer> customers() {
    return this.customerRepository.findAll();
  }

  @QueryMapping
  Flux<Customer> customersByName(@Argument String name) {
    return this.customerRepository.findByNameIgnoringCase(name);
  }

  @SchemaMapping(typeName = "Customer")
  Flux<Order> orders(Customer customer) {
    // Could be webservice call
    var orders = new ArrayList<Order>();
    for (var orderId = 1; orderId <= new SecureRandom().nextInt() * 100; orderId++) {
      orders.add(new Order(orderId, customer.id()));
    }
    return Flux.fromIterable(orders);
  }

  @MutationMapping
  Mono<Customer> addCustomer(@Argument String name) {
    return this.customerRepository.save(new Customer(null, name));
  }
}
