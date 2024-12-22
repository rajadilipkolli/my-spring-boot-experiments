package com.example.custom.sequence.repositories;

import com.example.custom.sequence.entities.Order;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface OrderRepository
        extends BaseJpaRepository<Order, String>, ListPagingAndSortingRepository<Order, String> {

    @Query("select o from Order o join fetch o.customer where o.id = :id")
    Optional<Order> findById(@Param("id") String id);

    void deleteAllInBatch();

    @EntityGraph(attributePaths = "customer")
    Optional<Order> findByIdAndCustomer_Id(String id, String customerId);
}
