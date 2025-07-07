package com.example.rest.webclient.config;

import com.example.rest.webclient.service.PostService;
import com.example.rest.webclient.utils.AppConstants;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(AppConstants.PROFILE_NOT_TEST)
public class Initializer implements CommandLineRunner {

    private final PostService postService;

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");
        postService
                .findAllPosts("id", "asc")
                .subscribe(
                        savedPost -> log.info("Retrieved Post {}", savedPost),
                        err -> log.error("Error Occurred while saving ", err),
                        () -> log.info("Initial Load successful"));
    }
}
