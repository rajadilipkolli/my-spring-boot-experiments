package com.example.restclient.bootrestclient.services;

import com.example.restclient.bootrestclient.exception.MyCustomRuntimeException;
import com.example.restclient.bootrestclient.model.response.PostDto;
import java.util.List;
import java.util.Optional;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PostService {

    private final RestClient restClient;

    public PostService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<PostDto> findAllPosts() {
        return restClient
                .get()
                .uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PostDto>>() {});
    }

    public Optional<PostDto> findPostById(Long id) {
        return Optional.ofNullable(
                restClient
                        .get()
                        .uri(uriBuilder -> uriBuilder.path("/posts/{postId}").build(id))
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .onStatus(
                                HttpStatusCode::is4xxClientError,
                                (request, response) -> {
                                    throw new MyCustomRuntimeException(
                                            response.getStatusCode(), response.getHeaders());
                                })
                        .body(PostDto.class));
    }

    public PostDto savePost(PostDto post) {
        return restClient
                .post()
                .uri("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(post)
                .retrieve()
                .body(PostDto.class);
    }

    public Optional<PostDto> updatePostById(Long id, PostDto postDto) {
        return Optional.ofNullable(
                restClient
                        .put()
                        .uri(uriBuilder -> uriBuilder.path("/posts/{postId}").build(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(postDto)
                        .retrieve()
                        .onStatus(
                                HttpStatusCode::is4xxClientError,
                                (request, response) -> {
                                    throw new MyCustomRuntimeException(
                                            response.getStatusCode(), response.getHeaders());
                                })
                        .body(PostDto.class));
    }

    public String deletePostById(Long id) {
        return restClient
                .delete()
                .uri(uriBuilder -> uriBuilder.path("/posts/{postId}").build(id))
                .retrieve()
                .body(String.class);
    }
}
