package com.example.rest.webclient.web.controllers;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.is;

import com.example.rest.webclient.common.AbstractIntegrationTest;
import com.example.rest.webclient.model.PostDto;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class PostControllerIT extends AbstractIntegrationTest {

    private WireMockServer wireMockServer;

    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(9091);
        wireMockServer.start();
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void findAllPosts() {
        // Prepare mock data
        List<PostDto> mockPosts =
                List.of(
                        new PostDto(1L, "Title 1", 1L, "Content 1"),
                        new PostDto(2L, "Title 2", 1L, "Content 2"),
                        new PostDto(3L, "Title 3", 1L, "Content 3"));
        wireMockServer.stubFor(
                get(urlEqualTo("/posts"))
                        .willReturn(
                                aResponse()
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE)
                                        .withBody(
                                                this.objectMapper.writeValueAsString(mockPosts))));

        // Test method
        Flux<PostDto> result =
                this.webTestClient
                        .get()
                        .uri("/api/posts")
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus()
                        .isOk()
                        .returnResult(PostDto.class)
                        .getResponseBody();

        StepVerifier.create(result)
                .expectNextMatches(post -> post.id() == 1)
                .expectNextMatches(post -> post.id() == 2)
                .expectNextMatches(post -> post.id() == 3)
                .verifyComplete();

        wireMockServer.verify(getRequestedFor(urlEqualTo("/posts")));
    }

    @Test
    void shouldFindPostById() {
        Long postId = 1L;
        PostDto post = new PostDto(postId, "text 1", 1L, "First Body");

        wireMockServer.stubFor(
                get(urlEqualTo("/posts/1"))
                        .willReturn(
                                aResponse()
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE)
                                        .withBody(this.objectMapper.writeValueAsString(post))));

        this.webTestClient
                .get()
                .uri("/api/posts/{id}", postId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PostDto.class)
                .value(PostDto::title, is(post.title()))
                .value(PostDto::body, is(post.body()))
                .value(PostDto::id, is(post.id()))
                .value(PostDto::userId, is(post.userId()));

        wireMockServer.verify(getRequestedFor(urlEqualTo("/posts/1")));
    }
}
