package com.example.multitenancy.primary.repositories;

import com.example.multitenancy.primary.entities.PrimaryCustomer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("primaryCustomerRepository")
public interface PrimaryCustomerRepository extends JpaRepository<PrimaryCustomer, Long> {}
