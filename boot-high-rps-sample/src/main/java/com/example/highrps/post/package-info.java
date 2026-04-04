@ApplicationModule(
        displayName = "Post Management",
        type = ApplicationModule.Type.OPEN,
        allowedDependencies = {"author", "shared", "infrastructure", "postcomment"})
package com.example.highrps.post;

import org.springframework.modulith.ApplicationModule;
