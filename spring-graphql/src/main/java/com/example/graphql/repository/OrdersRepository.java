package com.example.graphql.repository;

import com.example.graphql.dtos.Orders;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OrdersRepository extends ReactiveCrudRepository<Orders, Integer> {
  Flux<Orders> findByCustomerId(Integer customerId);
}
