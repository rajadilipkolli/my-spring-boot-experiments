package com.example.graphql.repository;

import com.example.graphql.dtos.Customer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface CustomerRepository extends ReactiveCrudRepository<Customer, Integer> {

  Flux<Customer> findByNameIgnoringCase(String name);
}
