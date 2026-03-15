/**
 * Listener Module
 *
 * <p>
 * Contains JPA and Redis repository implementations. Marked OPEN to allow
 * other modules to reference repository types where necessary.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Listener",
        type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.example.highrps.listener;
