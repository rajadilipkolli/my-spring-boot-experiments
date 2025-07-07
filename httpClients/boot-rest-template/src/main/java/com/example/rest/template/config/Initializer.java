package com.example.rest.template.config;

import com.example.rest.template.entities.Post;
import com.example.rest.template.httpclient.RestHandler;
import com.example.rest.template.model.request.ApplicationRestRequest;
import com.example.rest.template.model.response.ApplicationRestResponse;
import com.example.rest.template.repositories.PostRepository;
import java.util.List;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
public class Initializer implements CommandLineRunner {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Initializer.class);
    private final PostRepository postRepository;
    private final RestHandler restHandler;

    public Initializer(PostRepository postRepository, RestHandler restHandler) {
        this.postRepository = postRepository;
        this.restHandler = restHandler;
    }

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

        Post entity = postApplicationRestResponse.body();
        entity.setId(null);
        this.postRepository.save(entity);

        List<Post> response =
                restHandler.getBody(
                        ApplicationRestRequest.builder()
                                .httpBaseUrl("https://jsonplaceholder.typicode.com/posts")
                                .build(),
                        new ParameterizedTypeReference<List<Post>>() {});
        List<Post> modifiedPostList = response.stream().peek(post -> post.setId(null)).toList();
        this.postRepository.saveAll(modifiedPostList);
    }
}
