/**
 * Shared Kernel Module
 *
 * <p>
 * This module contains cross-cutting utilities and base classes:
 * - Common exceptions (DomainException, ResourceNotFoundException)
 * - Base entities (BaseEntity, Auditable)
 * - Utilities (AssertUtil, IdGenerator)
 * - Application properties (AppProperties)
 * - Global exception handling
 *
 * <p>
 * This is a non-domain module accessible by all other modules.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Shared Kernel",
        type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.example.highrps.shared;
