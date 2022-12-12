package com.example.rest.template.config;

import com.example.rest.template.entities.Post;
import com.example.rest.template.httpclient.RestHandler;
import com.example.rest.template.model.request.ApplicationRestRequest;
import com.example.rest.template.model.response.ApplicationRestResponse;
import com.example.rest.template.repositories.PostRepository;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Initializer implements CommandLineRunner {

    private final PostRepository postRepository;
    private final RestHandler restHandler;

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");

        ApplicationRestResponse<Post[]> response =
                restHandler.get(
                        ApplicationRestRequest.builder()
                                .httpBaseUrl("https://jsonplaceholder.typicode.com/posts")
                                .build(),
                        Post[].class);

        this.postRepository.saveAll(Stream.of(response.body()).toList());
    }
}
