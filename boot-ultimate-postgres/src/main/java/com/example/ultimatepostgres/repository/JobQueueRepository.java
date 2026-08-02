package com.example.ultimatepostgres.repository;

import com.example.ultimatepostgres.model.JobQueueEntity;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobQueueRepository extends JpaRepository<JobQueueEntity, Long> {

    List<JobQueueEntity> findTop100ByOrderByCreatedAtDesc();

    @Query(
            value = "SELECT * FROM job_queue WHERE status = 'PENDING' AND available_at <= :now "
                    + "ORDER BY priority DESC, id ASC LIMIT :batchSize FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    List<JobQueueEntity> claimJobs(@Param("now") OffsetDateTime now, @Param("batchSize") int batchSize);

    @Modifying
    @Query("UPDATE JobQueueEntity j SET j.status = 'IN_PROGRESS', j.updatedAt = CURRENT_TIMESTAMP WHERE j.id IN :ids")
    int markInProgress(@Param("ids") List<Long> ids);

    @Modifying
    @Query("UPDATE JobQueueEntity j SET j.status = 'COMPLETED', j.updatedAt = CURRENT_TIMESTAMP WHERE j.id = :id")
    int markCompleted(@Param("id") Long id);

    @Modifying
    @Query("UPDATE JobQueueEntity j SET j.status = 'FAILED', j.updatedAt = CURRENT_TIMESTAMP WHERE j.id = :id")
    int markFailed(@Param("id") Long id);

    @Modifying
    @Query("UPDATE JobQueueEntity j SET j.status = 'PENDING', j.attemptCount = j.attemptCount + 1, "
            + "j.availableAt = :retryAt, j.updatedAt = CURRENT_TIMESTAMP WHERE j.id = :id AND j.status = 'FAILED'")
    int resetFailedToPending(@Param("id") Long id, @Param("retryAt") OffsetDateTime retryAt);

    @Modifying
    @Query("UPDATE JobQueueEntity j SET j.status = 'PENDING', j.attemptCount = j.attemptCount + 1, "
            + "j.availableAt = :retryAt, j.updatedAt = CURRENT_TIMESTAMP WHERE j.id = :id")
    int scheduleRetry(@Param("id") Long id, @Param("retryAt") OffsetDateTime retryAt);
}
