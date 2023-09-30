package com.example.jooq.r2dbc.router;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jooq.r2dbc.common.AbstractIntegrationTest;
import com.example.jooq.r2dbc.model.request.CreatePostCommand;
import com.example.jooq.r2dbc.repository.TagRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class WebRouterConfigIT extends AbstractIntegrationTest {

    @Autowired private TagRepository tagRepository;

    @Test
    void willLoadPosts() {
        this.webTestClient
                .get()
                .uri("/posts")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$[*].title")
                .value((List<String> titles) -> assertThat(titles).containsAnyOf("jooq test"));
    }

    @Test
    void willCreatePosts() {
        CreatePostCommand createPost =
                new CreatePostCommand("junitTitle", "junitContent", List.of("junitTag"));
        Mono<Long> count = tagRepository.count();
        this.webTestClient
                .post()
                .uri("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createPost)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .exists("Location")
                .expectBody(UUID.class);

        createPost = new CreatePostCommand("junitTitle1", "junitContent1", List.of("junitTag"));
        this.webTestClient
                .post()
                .uri("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createPost)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectHeader()
                .exists("Location")
                .expectBody(UUID.class);

        Mono<Long> newCount = tagRepository.count();

        Mono<Long> resultMono = newCount.zipWith(newCount, (value1, value2) -> value2 - value1);

        // Use StepVerifier to assert the behavior
        StepVerifier.create(resultMono)
                .expectNext(0L) // Expected result after subtraction
                .expectComplete()
                .verify();
    }
}
