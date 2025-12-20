package com.example.jooq.r2dbc.router;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jooq.r2dbc.common.AbstractIntegrationTest;
import com.example.jooq.r2dbc.model.request.CreatePostCommand;
import com.example.jooq.r2dbc.model.response.PaginatedResult;
import com.example.jooq.r2dbc.repository.TagRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

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
                .value((List<String> titles) -> assertThat(titles).contains("jooq test"));
    }

    @Test
    void searchPosts() {
        this.webTestClient
                .get()
                .uri(
                        uriBuilder -> {
                            uriBuilder.path("/posts/search");
                            uriBuilder.queryParam("keyword", "Jooq");
                            return uriBuilder.build();
                        })
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(PaginatedResult.class)
                .value(
                        paginatedResult -> {
                            assertThat(paginatedResult.data()).isNotEmpty().hasSize(1);
                            assertThat(paginatedResult.totalElements()).isOne();
                            assertThat(paginatedResult.pageNumber()).isOne();
                            assertThat(paginatedResult.totalPages()).isOne();
                            assertThat(paginatedResult.isFirst()).isTrue();
                            assertThat(paginatedResult.isLast()).isTrue();
                            assertThat(paginatedResult.hasNext()).isFalse();
                            assertThat(paginatedResult.hasPrevious()).isFalse();
                        });
    }

    @Test
    void willCreatePosts() {
        CreatePostCommand createPost =
                new CreatePostCommand("junitTitle", "junitContent", List.of("junitTag"));

        // Save the initial count of tags
        Long initialCount = tagRepository.count().block();

        // Create the first post
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

        // Calculate the difference in tag count after creating posts
        Long afterCount = tagRepository.count().block();

        // Use Assertions to verify the result
        assertThat(afterCount - initialCount).isOne();

        // Create the second post
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

        // Calculate the difference in tag count after creating posts
        afterCount = tagRepository.count().block();

        // Use Assertions to verify the result that no newer one is created
        assertThat(afterCount - initialCount).isOne();
    }
}
