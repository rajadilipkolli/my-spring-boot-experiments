package com.example.rest.webclient.config;

import com.example.rest.webclient.mapper.PostMapper;
import com.example.rest.webclient.repository.PostRepository;
import com.example.rest.webclient.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Initializer implements CommandLineRunner {

    private final PostService postService;
    private final PostRepository postRepository;
    private final PostMapper postMapper;

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");
        postService
                .findAllPosts("id", "asc")
                .map(this.postMapper::toEntity)
                .flatMap(this.postRepository::save)
                .subscribe(
                        savedPost -> log.info("Saved Post {}", savedPost),
                        err -> log.error("Error Occurred while saving ", err),
                        () -> log.info("Initial Load successful"));
    }
}
