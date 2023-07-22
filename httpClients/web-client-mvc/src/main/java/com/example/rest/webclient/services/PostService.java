package com.example.rest.webclient.services;

import com.example.rest.webclient.model.response.PostDto;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class PostService {

    private final WebClient webClient;

    public List<PostDto> findAllPosts() {
        return webClient
                .get()
                .uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<List<PostDto>>() {})
                .blockLast();
    }

    public Optional<PostDto> findPostById(Long id) {
        return Optional.ofNullable(
                webClient
                        .get()
                        .uri(uriBuilder -> uriBuilder.path("/posts/{postId}").build(id))
                        .retrieve()
                        .bodyToMono(PostDto.class)
                        .block());
    }

    public PostDto savePost(PostDto post) {
        return webClient
                .post()
                .uri("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(post))
                .retrieve()
                .bodyToMono(PostDto.class)
                .block();
    }

    public PostDto deletePostById(Long id) {
        return webClient
                .delete()
                .uri(uriBuilder -> uriBuilder.path("/posts/{postId}").build(id))
                .retrieve()
                .bodyToMono(PostDto.class)
                .block();
    }
}
