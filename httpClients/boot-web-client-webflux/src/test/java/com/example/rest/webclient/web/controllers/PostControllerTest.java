package com.example.rest.webclient.web.controllers;

import static com.example.rest.webclient.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.example.rest.webclient.model.PostDto;
import com.example.rest.webclient.service.PostService;
import com.example.rest.webclient.web.controller.PostController;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

@WebFluxTest(controllers = PostController.class)
@ActiveProfiles(PROFILE_TEST)
class PostControllerTest {

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private PostService postService;

    @Autowired private ObjectMapper objectMapper;

    private List<PostDto> postList;

    @BeforeEach
    void setUp() {
        this.postList = new ArrayList<>();
        this.postList.add(new PostDto(1L, "text 1", 1L, "First Body"));
        this.postList.add(new PostDto(2L, "text 2", 1L, "Second Body"));
        this.postList.add(new PostDto(3L, "text 3", 1L, "Third Body"));
    }

    @Test
    void shouldFetchAllPosts() {
        given(postService.findAllPosts("id", "asc")).willReturn(Flux.fromIterable(postList));

        this.webTestClient
                .get()
                .uri("/api/posts")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(PostDto.class)
                .hasSize(3);
    }

    @Test
    void shouldFindPostById() {
        Long postId = 1L;
        PostDto post = new PostDto(postId, "text 1", 1L, "First Body");
        given(postService.findPostById(postId)).willReturn(Mono.just(post));

        this.webTestClient
                .get()
                .uri("/api/posts/{id}", postId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PostDto.class)
                .value(PostDto::title, is(post.title()));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingPost() {
        Long postId = 1L;
        given(postService.findPostById(postId)).willReturn(Mono.empty());

        this.webTestClient
                .get()
                .uri("/api/posts/{id}", postId)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void shouldCreateNewPost() {
        given(postService.savePost(any(PostDto.class)))
                .willAnswer((invocation) -> Mono.just(invocation.getArgument(0)));

        PostDto post = new PostDto(1L, "some text", 1L, "First Body");
        this.webTestClient
                .post()
                .uri("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(post))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(PostDto.class)
                .value(PostDto::id, notNullValue())
                .value(PostDto::title, is(post.title()));
    }

    @Test
    void shouldReturn400WhenCreateNewPostWithoutTitle() {
        PostDto post = new PostDto(null, null, null, null);

        this.webTestClient
                .post()
                .uri("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(post))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(ProblemDetail.class)
                .value(ProblemDetail::getTitle, is("Validation failure"))
                .value(ProblemDetail::getStatus, is(400))
                .value(ProblemDetail::getDetail, is("Invalid request content."))
                .value(ProblemDetail::getInstance, is(URI.create("/api/posts")))
                .value(t -> t.getProperties().size(), is(1));
    }

    @Test
    void shouldUpdatePost() throws Exception {
        Long postId = 1L;
        PostDto post = new PostDto(postId, "Updated text", 1L, "First Body");
        given(postService.findPostById(postId)).willReturn(Mono.just(post));
        given(postService.savePost(any(PostDto.class)))
                .willAnswer((invocation) -> Mono.just(invocation.getArgument(0)));

        this.webTestClient
                .put()
                .uri("/api/posts/{id}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(post))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PostDto.class)
                .value(PostDto::title, is(post.title()));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingPost() throws Exception {
        Long postId = 1L;
        given(postService.findPostById(postId)).willReturn(Mono.empty());
        PostDto post = new PostDto(postId, "Updated text", 1L, "First Body");

        this.webTestClient
                .put()
                .uri("/api/posts/{id}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(post))
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void shouldDeletePost() {
        Long postId = 1L;
        PostDto post = new PostDto(postId, "Some text", 1L, "First Body");
        given(postService.findPostById(postId)).willReturn(Mono.just(post));
        given(postService.deletePostById(post.id())).willReturn(Mono.just(post));

        this.webTestClient
                .delete()
                .uri("/api/posts/{id}", postId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PostDto.class)
                .value(PostDto::title, is(post.title()));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingPost() {
        Long postId = 1L;
        given(postService.findPostById(postId)).willReturn(Mono.empty());

        this.webTestClient
                .delete()
                .uri("/api/posts/{id}", postId)
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}
