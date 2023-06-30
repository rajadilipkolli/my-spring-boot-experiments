package com.example.hibernatecache.repositories;

import com.example.hibernatecache.entities.Customer;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.hibernate.jpa.AvailableHints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.transaction.annotation.Transactional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Transactional(readOnly = true)
    @QueryHints(@QueryHint(name = AvailableHints.HINT_CACHEABLE, value = "true"))
    Optional<Customer> findByFirstName(String firstName);
}