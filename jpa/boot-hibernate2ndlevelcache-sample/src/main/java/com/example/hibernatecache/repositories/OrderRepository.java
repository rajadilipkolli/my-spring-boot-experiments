package com.example.hibernatecache.repositories;

import static org.hibernate.jpa.AvailableHints.HINT_CACHEABLE;

import com.example.hibernatecache.entities.OrderItem;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import jakarta.persistence.QueryHint;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository
        extends BaseJpaRepository<OrderItem, Long>, PagingAndSortingRepository<OrderItem, Long> {

    void deleteAll();

    @Query("select o from OrderItem o where o.order.id = :orderId")
    @QueryHints(@QueryHint(name = HINT_CACHEABLE, value = "true"))
    List<OrderItem> findByOrder_Id(@Param("orderId") Long orderId);

    @QueryHints(@QueryHint(name = HINT_CACHEABLE, value = "true"))
    @Query("select o.id from OrderItem o")
    List<Long> findAllOrderItemIds();
}
