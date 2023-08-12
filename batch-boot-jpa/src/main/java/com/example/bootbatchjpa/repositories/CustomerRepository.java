package com.example.bootbatchjpa.repositories;

import com.example.bootbatchjpa.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {}
