package com.example.rest.webclient.service;

import com.example.rest.webclient.model.PostDto;
import java.util.Comparator;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PostService {

    private final WebClient webClient;

    public PostService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<PostDto> findAllPosts(String sortBy, String sortDir) {

        // Create the Comparator based on sortDir and sortBy
        Comparator<PostDto> comparator;
        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = Comparator.comparing((PostDto o) -> sortBy).reversed();
        } else {
            comparator = Comparator.comparing((PostDto o) -> sortBy);
        }

        return webClient
                .get()
                .uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(PostDto.class)
                .sort(comparator);
    }

    public Mono<PostDto> findPostById(Long id) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/posts/{postId}").build(id))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PostDto.class);
    }

    public Mono<PostDto> savePost(PostDto post) {
        return webClient
                .post()
                .uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(post))
                .retrieve()
                .bodyToMono(PostDto.class);
    }

    public Mono<PostDto> deletePostById(Long id) {
        return webClient
                .delete()
                .uri(uriBuilder -> uriBuilder.path("/posts/{postId}").build(id))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PostDto.class);
    }
}
