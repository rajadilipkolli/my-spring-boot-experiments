package com.example.custom.sequence.repositories;

import com.example.custom.sequence.entities.Order;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, String> {

    @Query("select o from Order o join fetch o.customer where o.id = :id")
    Optional<Order> findById(@Param("id") String id);
}
