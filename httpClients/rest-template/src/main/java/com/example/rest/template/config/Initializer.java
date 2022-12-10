package com.example.rest.template.config;

import com.example.rest.template.entities.Post;
import com.example.rest.template.repositories.PostRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class Initializer implements CommandLineRunner {

    private final PostRepository postRepository;
    private final RestTemplate restTemplate;

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");
        ResponseEntity<List<Post>> exchange =
                restTemplate.exchange(
                        "https://jsonplaceholder.typicode.com/posts",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Post>>() {});
        this.postRepository.saveAll(exchange.getBody());
    }
}
