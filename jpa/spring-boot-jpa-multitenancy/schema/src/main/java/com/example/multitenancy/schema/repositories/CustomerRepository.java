package com.example.multitenancy.schema.repositories;

import com.example.multitenancy.schema.entities.Customer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {}
