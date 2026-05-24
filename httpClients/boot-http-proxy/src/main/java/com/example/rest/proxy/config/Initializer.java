package com.example.rest.proxy.config;

import com.example.rest.proxy.client.JsonPlaceholderService;
import com.example.rest.proxy.entities.Post;
import com.example.rest.proxy.repositories.PostRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
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
        // Ensure ID is null to allow auto-generation
        postList.forEach(post -> post.setId(null));

        this.postRepository.saveAll(postList);
        log.info("Completed Initializer.....");
    }
}
