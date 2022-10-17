package com.example.multitenancy.partition.repositories;

import com.example.multitenancy.partition.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {}
