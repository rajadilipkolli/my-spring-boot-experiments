package com.example.multitenancy.partition.repositories;

import com.example.multitenancy.partition.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("select p from Customer p where text = :text")
    Customer findJpqlByText(String text);

    @NativeQuery("select * from customers c where text = :text")
    Customer findSqlByText(String text);

    long countByTenant(String tenant);
}
