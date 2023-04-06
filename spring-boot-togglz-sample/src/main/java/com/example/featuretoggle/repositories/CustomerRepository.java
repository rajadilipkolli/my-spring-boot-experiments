package com.example.featuretoggle.repositories;

import com.example.featuretoggle.entities.Customer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {}
