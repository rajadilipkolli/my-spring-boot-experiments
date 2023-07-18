package com.poc.boot.rabbitmq.repository;

import com.poc.boot.rabbitmq.entities.TrackingState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TrackingStateRepository extends JpaRepository<TrackingState, Long> {

    long countByStatus(String status);

    @Transactional
    @Modifying
    @Query("update TrackingState t set t.status = :status where t.correlationId = :correlationId")
    int updateStatusByCorrelationId(
            @Param("status") String status, @Param("correlationId") String correlationId);
}
