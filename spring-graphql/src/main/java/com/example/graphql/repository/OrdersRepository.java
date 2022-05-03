package com.example.graphql.repository;

import com.example.graphql.dtos.Orders;
import java.util.List;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OrdersRepository extends ReactiveCrudRepository<Orders, Integer> {

    Flux<Orders> findByCustomerIdIn(List<Integer> orderIds);
}
