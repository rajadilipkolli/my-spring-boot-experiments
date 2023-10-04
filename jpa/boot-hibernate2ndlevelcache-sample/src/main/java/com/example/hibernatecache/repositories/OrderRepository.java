package com.example.hibernatecache.repositories;

import com.example.hibernatecache.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {}
