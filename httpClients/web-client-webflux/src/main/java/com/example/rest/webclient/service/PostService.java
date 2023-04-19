package com.example.rest.webclient.service;

import com.example.rest.webclient.model.PostDto;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class PostService {

    private final WebClient webClient;

    public Flux<PostDto> findAllPosts(String sortBy, String sortDir) {
        return webClient
                .get()
                .uri("/posts")
                .retrieve()
                .bodyToFlux(PostDto.class)
                .sort(Comparator.comparing(PostDto::id));
    }

    public Mono<PostDto> findPostById(Long id) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/posts/{postId}").build(id))
                .retrieve()
                .bodyToMono(PostDto.class);
    }

    public Mono<PostDto> savePost(PostDto post) {
        return webClient.post().uri("/posts").retrieve().bodyToMono(PostDto.class);
    }

    public Mono<PostDto> deletePostById(Long id) {
        return webClient
                .delete()
                .uri(uriBuilder -> uriBuilder.path("/posts/{postId}").build(id))
                .retrieve()
                .bodyToMono(PostDto.class);
    }
}
