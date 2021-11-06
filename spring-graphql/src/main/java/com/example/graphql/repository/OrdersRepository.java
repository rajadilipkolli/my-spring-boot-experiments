package com.example.graphql.repository;

import com.example.graphql.dtos.Orders;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface OrdersRepository extends ReactiveCrudRepository<Orders, Integer> {

  Flux<Orders> findByCustomerIdIn(List<Integer> orderIds);
}
