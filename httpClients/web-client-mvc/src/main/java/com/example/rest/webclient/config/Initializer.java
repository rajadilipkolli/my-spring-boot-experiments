package com.example.rest.webclient.config;

import com.example.rest.webclient.entities.Post;
import com.example.rest.webclient.repositories.PostRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class Initializer implements CommandLineRunner {

    private final PostRepository postRepository;
    private final WebClient webClient;

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");
        List<Post> response =
                webClient
                        .get()
                        .uri("/posts")
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<Post>>() {})
                        .block();
        this.postRepository.saveAll(response);
    }
}
