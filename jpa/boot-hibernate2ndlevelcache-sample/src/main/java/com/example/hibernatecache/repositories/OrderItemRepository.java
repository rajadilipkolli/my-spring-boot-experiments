package com.example.hibernatecache.repositories;

import com.example.hibernatecache.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {}
