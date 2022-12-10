package com.example.rest.webclient.service;

import com.example.rest.webclient.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PostService {

    private final WebClient webClient;

    public Flux<Post> findAllPosts(String sortBy, String sortDir) {
        return webClient.get().uri("/posts").retrieve().bodyToFlux(Post.class);
    }

    public Mono<Post> findPostById(Long id) {
        return webClient.get().uri("/posts/" + id).retrieve().bodyToMono(Post.class);
    }

    public Mono<Post> savePost(Post post) {
        return null;
    }

    public void deletePostById(Long id) {}
}
