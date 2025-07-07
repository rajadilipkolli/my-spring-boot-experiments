package com.example.rest.webclient.config;

import com.example.rest.webclient.service.PostService;
import com.example.rest.webclient.utils.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(AppConstants.PROFILE_NOT_TEST)
public class Initializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Initializer.class);

    private final PostService postService;

    public Initializer(PostService postService) {
        this.postService = postService;
    }

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
