package com.example.rest.webclient.web.controllers;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.example.rest.webclient.common.AbstractIntegrationTest;
import com.example.rest.webclient.model.PostDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class PostControllerIT extends AbstractIntegrationTest {

    private WireMockServer wireMockServer;

    @BeforeEach
    public void setup() {
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
    }

    @AfterEach
    public void teardown() {
        wireMockServer.stop();
    }

    @Test
    @Disabled
    void testFindAllPosts() throws JsonProcessingException {
        // Prepare mock data
        List<PostDto> mockPosts =
                Arrays.asList(
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

        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/posts")));
    }
}
