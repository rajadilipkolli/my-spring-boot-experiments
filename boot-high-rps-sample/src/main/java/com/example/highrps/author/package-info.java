@ApplicationModule(
        displayName = "Author Management",
        type = ApplicationModule.Type.OPEN,
        allowedDependencies = {"shared", "infrastructure", "post"})
package com.example.highrps.author;

import org.springframework.modulith.ApplicationModule;
