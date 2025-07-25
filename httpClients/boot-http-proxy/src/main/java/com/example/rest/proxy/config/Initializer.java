package com.example.rest.proxy.config;

import com.example.rest.proxy.client.JsonPlaceholderService;
import com.example.rest.proxy.entities.Post;
import com.example.rest.proxy.repositories.PostRepository;
import com.example.rest.proxy.utils.AppConstants;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(AppConstants.PROFILE_NOT_TEST)
class Initializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Initializer.class);

    private final JsonPlaceholderService jsonPlaceholderService;
    private final PostRepository postRepository;

    public Initializer(JsonPlaceholderService jsonPlaceholderService, PostRepository postRepository) {
        this.jsonPlaceholderService = jsonPlaceholderService;
        this.postRepository = postRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");
        List<Post> postList = jsonPlaceholderService.loadAllPosts();
        this.postRepository.saveAll(postList);
        log.info("Completed Initializer.....");
    }
}
