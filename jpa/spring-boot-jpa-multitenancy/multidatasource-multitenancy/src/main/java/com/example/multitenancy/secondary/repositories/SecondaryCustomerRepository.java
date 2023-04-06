package com.example.multitenancy.secondary.repositories;

import com.example.multitenancy.secondary.entities.SecondaryCustomer;

import org.springframework.stereotype.Repository;

@Repository("secondaryCustomerRepository")
public interface SecondaryCustomerRepository
        extends org.springframework.data.jpa.repository.JpaRepository<SecondaryCustomer, Long> {}
