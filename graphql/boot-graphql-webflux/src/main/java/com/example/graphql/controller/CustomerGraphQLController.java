package com.example.graphql.controller;

import com.example.graphql.dtos.Customer;
import com.example.graphql.dtos.Orders;
import com.example.graphql.service.CustomerGraphQLService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@Validated
@RequiredArgsConstructor
public class CustomerGraphQLController {

    private final CustomerGraphQLService customerGraphQLService;

    //    @SchemaMapping(typeName = "Query", field = "customers") or
    @QueryMapping
    Flux<Customer> customers() {
        return this.customerGraphQLService.findAllCustomers();
    }

    @QueryMapping
    Flux<Customer> customersByName(@Argument String name) {
        return this.customerGraphQLService.findByNameIgnoringCase(name);
    }

    //  @SchemaMapping(typeName = "Customer")
    //  Flux<Orders> orders(Customer customer) {
    //    return this.ordersRepository.findByCustomerId(customer.id());
    //  }

    // replacement for above code
    @BatchMapping(typeName = "Customer")
    public Mono<Map<Customer, List<Orders>>> orders(List<Customer> customers) {
        return this.customerGraphQLService.findAllOrdersByCustomers(customers);
    }

    @MutationMapping
    Mono<Customer> addCustomer(@Argument @NotBlank String name) {
        return this.customerGraphQLService.addCustomer(name);
    }

    @MutationMapping
    Mono<Orders> addOrderToCustomer(@Argument @Positive Integer id) {
        return this.customerGraphQLService.addOrderToCustomer(id);
    }
}
