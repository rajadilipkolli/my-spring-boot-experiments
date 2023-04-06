package com.example.rest.template.config;

import com.example.rest.template.entities.Post;
import com.example.rest.template.httpclient.RestHandler;
import com.example.rest.template.model.request.ApplicationRestRequest;
import com.example.rest.template.model.response.ApplicationRestResponse;
import com.example.rest.template.repositories.PostRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class Initializer implements CommandLineRunner {

    private final PostRepository postRepository;
    private final RestHandler restHandler;

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");

        ApplicationRestResponse<Post> postApplicationRestResponse =
                restHandler.get(
                        ApplicationRestRequest.builder()
                                .httpBaseUrl("https://jsonplaceholder.typicode.com/posts")
                                .path("/{postId}")
                                .pathVariables(Map.of("postId", 1))
                                .build(),
                        Post.class);

        this.postRepository.save(postApplicationRestResponse.body());

        List<Post> response =
                restHandler.getBody(
                        ApplicationRestRequest.builder()
                                .httpBaseUrl("https://jsonplaceholder.typicode.com/posts")
                                .build(),
                        new ParameterizedTypeReference<List<Post>>() {});

        this.postRepository.saveAll(response);
    }
}
