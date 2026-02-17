package com.example.keysetpagination.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing for automatic management of entity timestamps.
 * Note: JPA Auditing works based on Proxy, so proxy beans must remain enabled.
 *
 * This configuration supports:
 * - Automatic population of @CreatedDate in Auditable entities
 * - Primarily used by Animal entity for tracking creation timestamps
 */
@Configuration
@EnableJpaAuditing(modifyOnCreate = false)
public class JpaAuditConfig {}
