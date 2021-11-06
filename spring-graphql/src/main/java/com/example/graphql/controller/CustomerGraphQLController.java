package com.example.graphql.controller;

import com.example.graphql.dtos.Customer;
import com.example.graphql.dtos.Orders;
import com.example.graphql.repository.CustomerRepository;
import com.example.graphql.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Validated
@RequiredArgsConstructor
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

  //  @SchemaMapping(typeName = "Customer")
  //  Flux<Orders> orders(Customer customer) {
  //    return this.ordersRepository.findByCustomerId(customer.id());
  //  }

  // replacement for above code
  @BatchMapping(typeName = "Customer")
  public Mono<Map<Customer, List<Orders>>> orders(List<Customer> customers) {
    var keys = customers.stream().map(Customer::id).toList();
    return this.ordersRepository
        .findByCustomerIdIn(keys)
        .collectMultimap(Orders::customerId)
        .map(
            customerOrderMap -> {
              var result = new HashMap<Customer, List<Orders>>();
              customerOrderMap
                  .keySet()
                  .forEach(
                      customerId -> {
                        var customer =
                            customers.stream()
                                .filter(cust -> cust.id().equals(customerId))
                                .toList()
                                .get(0);
                        result.put(customer, new ArrayList<>(customerOrderMap.get(customerId)));
                      });
              return result;
            });
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
