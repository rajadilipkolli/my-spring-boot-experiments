package com.example.ultimatepostgres.repository;

import com.example.ultimatepostgres.model.CacheEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CacheRepository extends JpaRepository<CacheEntity, String> {

    @Query(value = "SELECT * FROM cache_entries WHERE key = :key AND expires_at > :now", nativeQuery = true)
    Optional<CacheEntity> findValidByKey(@Param("key") String key, @Param("now") OffsetDateTime now);

    @Query(
            value = "SELECT * FROM cache_entries WHERE key LIKE :prefix ESCAPE '\\' AND expires_at > :now",
            nativeQuery = true)
    List<CacheEntity> findValidByPrefix(@Param("prefix") String prefix, @Param("now") OffsetDateTime now);

    @Modifying
    @Query(
            value = "INSERT INTO cache_entries (key, value, expires_at, created_at, updated_at) "
                    + "VALUES (:key, :value\\:\\:jsonb, :expiresAt, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) "
                    + "ON CONFLICT (key) DO UPDATE SET "
                    + "value = EXCLUDED.value, "
                    + "expires_at = EXCLUDED.expires_at, "
                    + "updated_at = CURRENT_TIMESTAMP",
            nativeQuery = true)
    void upsert(@Param("key") String key, @Param("value") String value, @Param("expiresAt") OffsetDateTime expiresAt);

    @Modifying
    @Query(value = "DELETE FROM cache_entries WHERE expires_at <= :now", nativeQuery = true)
    int deleteExpired(@Param("now") OffsetDateTime now);

    @Modifying
    @Query(value = "TRUNCATE TABLE cache_entries", nativeQuery = true)
    void truncate();
}
