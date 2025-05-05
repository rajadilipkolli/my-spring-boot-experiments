package com.example.hibernatecache.repositories;

import static org.hibernate.jpa.AvailableHints.HINT_CACHEABLE;
import static org.hibernate.jpa.HibernateHints.HINT_JDBC_BATCH_SIZE;

import com.example.hibernatecache.entities.Customer;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

public interface CustomerRepository
        extends BaseJpaRepository<Customer, Long>, PagingAndSortingRepository<Customer, Long> {

    @QueryHints(@QueryHint(name = HINT_CACHEABLE, value = "true"))
    @EntityGraph(attributePaths = {"orders"})
    Optional<Customer> findByFirstName(String firstName);

    @Override
    @QueryHints(@QueryHint(name = HINT_CACHEABLE, value = "true"))
    @EntityGraph(attributePaths = {"orders"})
    Optional<Customer> findById(Long aLong);

    @QueryHints(@QueryHint(name = HINT_JDBC_BATCH_SIZE, value = "25"))
    @Query("delete from Customer ")
    @Transactional
    @Modifying
    void deleteAllInBatch();
}
