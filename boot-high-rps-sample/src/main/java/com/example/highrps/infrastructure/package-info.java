/**
 * Infrastructure Module
 *
 * <p>
 * This module provides cross-cutting infrastructure concerns for the
 * application:
 * - Caching (Caffeine, Redis)
 * - Kafka integration and batch processing
 * - Persistence configuration (JPA auditing)
 *
 * <p>
 * This is a non-domain module that supports all other modules.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Infrastructure",
        type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.example.highrps.infrastructure;
