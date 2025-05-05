package com.example.hibernatecache.repositories;

import static org.hibernate.jpa.AvailableHints.HINT_CACHEABLE;
import static org.hibernate.jpa.HibernateHints.HINT_JDBC_BATCH_SIZE;

import com.example.hibernatecache.entities.OrderItem;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import jakarta.persistence.QueryHint;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface OrderItemRepository
        extends BaseJpaRepository<OrderItem, Long>, PagingAndSortingRepository<OrderItem, Long> {

    @Query("select o from OrderItem o where o.order.id = :orderId")
    @QueryHints(@QueryHint(name = HINT_CACHEABLE, value = "true"))
    List<OrderItem> findByOrder_Id(@Param("orderId") Long orderId);

    @QueryHints(@QueryHint(name = HINT_JDBC_BATCH_SIZE, value = "25"))
    @Query("delete from OrderItem ")
    @Transactional
    @Modifying
    void deleteAllInBatch();
}
