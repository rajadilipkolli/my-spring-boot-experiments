package com.example.custom.sequence.repositories;

import com.example.custom.sequence.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {}
