package com.example.rest.webclient.config;

import com.example.rest.webclient.model.Post;
import com.example.rest.webclient.repository.PostRepository;
import com.example.rest.webclient.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
@Slf4j
public class Initializer implements CommandLineRunner {

    private final PostService postService;
    private final PostRepository postRepository;

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");
        Flux<Post> response = postService.findAllPosts("id", "asc");
        this.postRepository.saveAll(response).subscribe();
    }
}
