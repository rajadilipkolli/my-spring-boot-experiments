package com.example.hibernatecache.repositories;

import com.example.hibernatecache.entities.Order;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface OrderRepository extends BaseJpaRepository<Order, Long>, PagingAndSortingRepository<Order, Long> {

    void deleteAll();

    @Query("SELECT o FROM Order o join fetch o.orderItems oi WHERE o.id = :id ORDER BY oi.itemCode")
    Optional<Order> findById(Long id);
}
