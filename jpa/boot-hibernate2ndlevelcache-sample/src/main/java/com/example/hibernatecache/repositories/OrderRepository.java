package com.example.hibernatecache.repositories;

import static org.hibernate.jpa.AvailableHints.HINT_CACHEABLE;
import static org.hibernate.jpa.HibernateHints.HINT_JDBC_BATCH_SIZE;

import com.example.hibernatecache.entities.Order;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface OrderRepository extends BaseJpaRepository<Order, Long>, PagingAndSortingRepository<Order, Long> {

    @QueryHints(@QueryHint(name = HINT_CACHEABLE, value = "true"))
    @Query("SELECT o FROM Order o join fetch o.orderItems oi WHERE o.id = :id ORDER BY oi.itemCode")
    Optional<Order> findById(@Param("id") Long id);

    @QueryHints(@QueryHint(name = HINT_CACHEABLE, value = "true"))
    @Query("SELECT o FROM Order o join fetch o.orderItems oi WHERE o.customer.id = :customerId ORDER BY oi.itemCode")
    List<Order> findByCustomerId(@Param("customerId") Long customerId);

    @Override
    @Transactional
    @Modifying
    @Query("delete from Order o where o.id = :id")
    void deleteById(@Param("id") Long id);

    @QueryHints(@QueryHint(name = HINT_JDBC_BATCH_SIZE, value = "25"))
    @Query("delete from Order")
    @Transactional
    @Modifying
    void deleteAllInBatch();
}
