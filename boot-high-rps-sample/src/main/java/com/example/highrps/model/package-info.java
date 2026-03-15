/**
 * Model Module - DTOs for API requests and responses
 *
 * <p>
 * This module contains Data Transfer Objects (DTOs) used across the
 * application.
 * It is marked as OPEN to allow all modules to access these types.
 *
 * <p>
 * Note: This is a transitional module. In the future, these DTOs should be
 * moved
 * to their respective domain modules (post, author, postcomment).
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Model",
        type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.example.highrps.model;
