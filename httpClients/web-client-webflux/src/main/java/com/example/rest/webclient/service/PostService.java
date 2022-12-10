package com.example.rest.webclient.service;

import com.example.rest.webclient.model.Post;
import java.util.Comparator;
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
        return webClient
                .get()
                .uri("/posts")
                .retrieve()
                .bodyToFlux(Post.class)
                .sort(Comparator.comparing(Post::id));
    }

    public Mono<Post> findPostById(Long id) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/posts/{postId}").build(id))
                .retrieve()
                .bodyToMono(Post.class);
    }

    public Mono<Post> savePost(Post post) {
        return webClient.post().uri("/posts").retrieve().bodyToMono(Post.class);
    }

    public Mono<Post> deletePostById(Long id) {
        return webClient
                .delete()
                .uri(uriBuilder -> uriBuilder.path("/posts/{postId}").build(id))
                .retrieve()
                .bodyToMono(Post.class);
    }
}
