package com.example.multitenancy.schema.repositories;

import com.example.multitenancy.schema.entities.Customer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c ORDER BY c.id")
    @NonNull
    List<Customer> findAll();
}
