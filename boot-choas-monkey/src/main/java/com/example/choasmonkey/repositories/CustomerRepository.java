package com.example.choasmonkey.repositories;

import com.example.choasmonkey.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {}
