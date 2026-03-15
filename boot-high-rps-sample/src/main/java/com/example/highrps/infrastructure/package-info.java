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
@ApplicationModule(displayName = "Infrastructure", type = ApplicationModule.Type.OPEN)
package com.example.highrps.infrastructure;

import org.springframework.modulith.ApplicationModule;
