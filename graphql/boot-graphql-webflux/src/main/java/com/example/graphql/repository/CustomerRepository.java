package com.example.graphql.repository;

import com.example.graphql.dtos.Customer;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface CustomerRepository extends ReactiveCrudRepository<Customer, Integer> {

    @Query("SELECT * FROM customer ORDER BY id LIMIT :limit OFFSET :offset")
    Flux<Customer> findAllWithPaging(@Param("offset") int offset, @Param("limit") int limit);

    Flux<Customer> findByNameIgnoringCase(String name);
}
