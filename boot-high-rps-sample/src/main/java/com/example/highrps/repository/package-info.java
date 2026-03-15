/**
 * Repository Module
 *
 * <p>
 * Contains JPA and Redis repository implementations. Marked OPEN to allow
 * other modules to reference repository types where necessary.
 */
@ApplicationModule(displayName = "Repository", type = ApplicationModule.Type.OPEN)
package com.example.highrps.repository;

import org.springframework.modulith.ApplicationModule;
