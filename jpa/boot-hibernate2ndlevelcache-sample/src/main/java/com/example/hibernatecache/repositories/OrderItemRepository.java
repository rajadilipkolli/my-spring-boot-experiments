package com.example.hibernatecache.repositories;

import com.example.hibernatecache.entities.OrderItem;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import io.hypersistence.utils.spring.repository.HibernateRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface OrderItemRepository
        extends BaseJpaRepository<OrderItem, Long>,
                HibernateRepository<OrderItem>,
                PagingAndSortingRepository<OrderItem, Long> {

    void deleteAll();
}
