package com.example.custom.sequence.repositories;

import com.example.custom.sequence.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, String> {}
