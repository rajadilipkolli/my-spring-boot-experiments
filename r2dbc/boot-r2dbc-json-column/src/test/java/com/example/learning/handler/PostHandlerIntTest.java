package com.example.learning.handler;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.learning.common.AbstractIntegrationTest;
import com.example.learning.entity.Post;
import com.example.learning.model.response.PagedResult;
import com.example.learning.repository.PostRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;

class PostHandlerIntTest extends AbstractIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        // Create test data
        var posts = IntStream.range(1, 21) // Create 20 posts
                .mapToObj(i -> Post.builder()
                        .title("Test Post " + i)
                        .content("Content " + i)
                        .build())
                .collect(Collectors.toList());

        // Clear existing data and save new test data
        postRepository.deleteAll().thenMany(postRepository.saveAll(posts)).blockLast();
    }

    @Test
    void shouldReturnDefaultPagination() {
        this.webTestClient
                .get()
                .uri("/posts")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PagedResult<Post>>() {})
                .value(pagedResult -> {
                    assertThat(pagedResult.totalElements()).isEqualTo(20);
                    assertThat(pagedResult.pageNumber()).isEqualTo(1);
                    assertThat(pagedResult.totalPages()).isEqualTo(2);
                    assertThat(pagedResult.isFirst()).isEqualTo(true);
                    assertThat(pagedResult.isLast()).isEqualTo(false);
                    assertThat(pagedResult.hasNext()).isEqualTo(true);
                    assertThat(pagedResult.hasPrevious()).isEqualTo(false);
                    List<Post> posts = pagedResult.data();
                    assertThat(posts).isNotNull().isNotEmpty().hasSize(10);
                });
    }

    @Test
    void shouldReturnCustomPageSize() {
        this.webTestClient
                .get()
                .uri("/posts?page=0&size=5")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PagedResult<Post>>() {})
                .value(pagedResult -> {
                    assertThat(pagedResult.totalElements()).isEqualTo(20);
                    assertThat(pagedResult.pageNumber()).isEqualTo(1);
                    assertThat(pagedResult.totalPages()).isEqualTo(4);
                    assertThat(pagedResult.isFirst()).isEqualTo(true);
                    assertThat(pagedResult.isLast()).isEqualTo(false);
                    assertThat(pagedResult.hasNext()).isEqualTo(true);
                    assertThat(pagedResult.hasPrevious()).isEqualTo(false);
                    List<Post> posts = pagedResult.data();
                    assertThat(posts).isNotNull().isNotEmpty().hasSize(5);
                });
    }

    @Test
    void shouldReturnSecondPage() {
        this.webTestClient
                .get()
                .uri("/posts?page=1&size=8")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PagedResult<Post>>() {})
                .value(pagedResult -> {
                    assertThat(pagedResult.totalElements()).isEqualTo(20);
                    assertThat(pagedResult.pageNumber()).isEqualTo(1);
                    assertThat(pagedResult.totalPages()).isEqualTo(3);
                    assertThat(pagedResult.isFirst()).isEqualTo(true);
                    assertThat(pagedResult.isLast()).isEqualTo(false);
                    assertThat(pagedResult.hasNext()).isEqualTo(true);
                    assertThat(pagedResult.hasPrevious()).isEqualTo(false);
                    List<Post> posts = pagedResult.data();
                    assertThat(posts).isNotNull().isNotEmpty().hasSize(8);
                });
    }

    @Test
    void shouldReturnEmptyContentForOutOfBoundsPage() {
        this.webTestClient
                .get()
                .uri("/posts?page=10&size=10")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PagedResult<Post>>() {})
                .value(pagedResult -> {
                    assertThat(pagedResult.totalElements()).isEqualTo(20);
                    assertThat(pagedResult.pageNumber()).isEqualTo(1);
                    assertThat(pagedResult.totalPages()).isEqualTo(2);
                    assertThat(pagedResult.isFirst()).isEqualTo(true);
                    assertThat(pagedResult.isLast()).isEqualTo(false);
                    assertThat(pagedResult.hasNext()).isEqualTo(true);
                    assertThat(pagedResult.hasPrevious()).isEqualTo(false);
                    List<Post> posts = pagedResult.data();
                    assertThat(posts).isNull();
                });
    }
}
