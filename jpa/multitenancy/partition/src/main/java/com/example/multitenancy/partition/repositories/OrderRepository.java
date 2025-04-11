package com.example.multitenancy.partition.repositories;

import com.example.multitenancy.partition.entities.Order;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);
}
