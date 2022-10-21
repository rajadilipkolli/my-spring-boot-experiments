package com.example.multitenancy.partition.repositories;

import com.example.multitenancy.partition.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("select p from Customer p where text = :text")
    Customer findJpqlByText(String text);

    @Query(value = "select * from customers c where text = :text", nativeQuery = true)
    Customer findSqlByText(String text);
}
