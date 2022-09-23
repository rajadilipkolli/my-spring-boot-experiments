package com.example.hibernatecache.repositories;

import com.example.hibernatecache.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {}
