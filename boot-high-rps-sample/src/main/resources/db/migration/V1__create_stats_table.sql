-- Flyway migration: create stats_entity table matching StatsEntity JPA mapping
CREATE TABLE IF NOT EXISTS stats_entity (
  id VARCHAR(255) PRIMARY KEY,
  stat_value BIGINT NOT NULL
);
