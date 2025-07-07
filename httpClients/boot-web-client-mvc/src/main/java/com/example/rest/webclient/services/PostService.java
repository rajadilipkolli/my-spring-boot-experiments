package com.example.rest.webclient.services;

import com.example.rest.webclient.model.response.PostDto;
import java.util.List;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class PostService {

    private final WebClient webClient;

    public PostService(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<PostDto> findAllPosts() {
        return webClient
                .get()
                .uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(PostDto.class)
                .collectList()
                .block();
    }

    public Optional<PostDto> findPostById(Long id) {
        return Optional.ofNullable(
                webClient
                        .get()
                        .uri(uriBuilder -> uriBuilder.path("/posts/{postId}").build(id))
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(PostDto.class)
                        .block());
    }

    public PostDto savePost(PostDto post) {
        return webClient
                .post()
                .uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(post))
                .retrieve()
                .bodyToMono(PostDto.class)
                .block();
    }

    public Optional<PostDto> updatePostById(Long id, PostDto post) {
        return Optional.ofNullable(
                webClient
                        .put()
                        .uri(uriBuilder -> uriBuilder.path("/posts/{postId}").build(id))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(post))
                        .retrieve()
                        .bodyToMono(PostDto.class)
                        .block());
    }

    public PostDto deletePostById(Long id) {
        return webClient
                .delete()
                .uri(uriBuilder -> uriBuilder.path("/posts/{postId}").build(id))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PostDto.class)
                .block();
    }
}
