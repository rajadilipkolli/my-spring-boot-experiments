package com.example.graphql.controller;

import com.example.graphql.dtos.Customer;
import com.example.graphql.dtos.Orders;
import com.example.graphql.repository.CustomerRepository;
import com.example.graphql.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Controller
@RequiredArgsConstructor
@Valid
public class CustomerGraphQLController {

  private final CustomerRepository customerRepository;
  private final OrdersRepository ordersRepository;

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
  Flux<Orders> orders(Customer customer) {
    return this.ordersRepository.findByCustomerId(customer.id());
  }

  @MutationMapping
  Mono<Customer> addCustomer(@Argument @NotBlank String name) {
    return this.customerRepository.save(new Customer(null, name));
  }

  @MutationMapping
  Mono<Orders> addOrderToCustomer(@Argument @Positive Integer id) {
    return this.ordersRepository.save(new Orders(null, id));
  }
}
