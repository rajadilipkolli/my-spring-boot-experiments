package com.example.multitenancy.partition.repositories;

import com.example.multitenancy.partition.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {}
