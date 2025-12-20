package com.example.custom.sequence.repositories;

import com.example.custom.sequence.entities.Customer;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends BaseJpaRepository<Customer, String> {

    @EntityGraph(attributePaths = "orders")
    Optional<Customer> findById(String id);

    @Query("""
                select c
                from Customer c
                left join fetch c.orders
                where c.id in :customerIds
            """)
    List<Customer> findAllByIdWithOrders(@Param("customerIds") List<String> customerIds);

    @Query(value = "select c.id from Customer c ", countQuery = "select count(c) from Customer c")
    Page<String> findAllCustomerIds(Pageable pageable);

    void deleteAllInBatch();
}
