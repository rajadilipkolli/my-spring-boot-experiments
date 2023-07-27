package com.example.envers.repositories;

import com.example.envers.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

public interface CustomerRepository
        extends JpaRepository<Customer, Long>, RevisionRepository<Customer, Long, Integer> {}
