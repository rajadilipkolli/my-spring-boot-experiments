@ApplicationModule(
        displayName = "Author Management",
        type = ApplicationModule.Type.OPEN,
        allowedDependencies = {
            "shared",
            "infrastructure",
            "infrastructure::cache",
            "infrastructure::redis",
            "infrastructure::kafka"
        })
package com.example.highrps.author;

import org.springframework.modulith.ApplicationModule;
