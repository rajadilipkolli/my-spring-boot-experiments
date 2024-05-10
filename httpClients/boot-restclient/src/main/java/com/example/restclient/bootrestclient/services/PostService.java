package com.example.restclient.bootrestclient.services;

import com.example.restclient.bootrestclient.model.response.PostDto;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private final HttpClientService httpClientService;

    public PostService(HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

    public List<PostDto> findAllPosts() {
        return httpClientService.callAndFetchResponseForGetMethod(
                uriBuilder -> uriBuilder.path("/posts").build(),
                new ParameterizedTypeReference<List<PostDto>>() {});
    }

    public Optional<PostDto> findPostById(Long id) {
        PostDto response =
                httpClientService.callAndFetchResponseForGetMethod(
                        uriBuilder -> uriBuilder.path("/posts/{postId}").build(id),
                        Map.of("apiKey", "123456"),
                        PostDto.class);
        return Optional.ofNullable(response);
    }

    public PostDto savePost(PostDto post) {
        return httpClientService.callAndFetchResponseForPostMethod(
                uriBuilder -> uriBuilder.path("/posts").build(), post, PostDto.class);
    }

    public Optional<PostDto> updatePostById(Long id, PostDto postDto) {
        PostDto response =
                httpClientService.callAndFetchResponseForPutMethod(
                        uriBuilder -> uriBuilder.path("/posts/{postId}").build(id),
                        postDto,
                        PostDto.class);
        return Optional.ofNullable(response);
    }

    public String deletePostById(Long id) {
        return httpClientService.callAndFetchResponseForDeleteMethod(
                uriBuilder -> uriBuilder.path("/posts/{postId}").build(id));
    }
}
