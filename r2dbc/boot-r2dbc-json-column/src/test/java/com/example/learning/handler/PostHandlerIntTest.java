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
import reactor.test.StepVerifier;

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
        StepVerifier.create(postRepository
                        .deleteAll()
                        .thenMany(postRepository.saveAll(posts))
                        .collectList())
                .expectNextCount(1)
                .verifyComplete();
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
                    assertThat(pagedResult.pageNumber()).isOne();
                    assertThat(pagedResult.totalPages()).isEqualTo(2);
                    assertThat(pagedResult.isFirst()).isTrue();
                    assertThat(pagedResult.isLast()).isFalse();
                    assertThat(pagedResult.hasNext()).isTrue();
                    assertThat(pagedResult.hasPrevious()).isFalse();
                    List<Post> posts = pagedResult.data();
                    assertThat(posts).isNotEmpty().hasSize(10);
                    // Verify content and order
                    assertThat(posts)
                            .extracting(Post::getTitle)
                            .containsExactly(
                                    "Test Post 1",
                                    "Test Post 2",
                                    "Test Post 3",
                                    "Test Post 4",
                                    "Test Post 5",
                                    "Test Post 6",
                                    "Test Post 7",
                                    "Test Post 8",
                                    "Test Post 9",
                                    "Test Post 10");
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
                    assertThat(pagedResult.pageNumber()).isOne();
                    assertThat(pagedResult.totalPages()).isEqualTo(4);
                    assertThat(pagedResult.isFirst()).isTrue();
                    assertThat(pagedResult.isLast()).isFalse();
                    assertThat(pagedResult.hasNext()).isTrue();
                    assertThat(pagedResult.hasPrevious()).isFalse();
                    List<Post> posts = pagedResult.data();
                    assertThat(posts).isNotEmpty().hasSize(5);
                    // Verify content
                    assertThat(posts)
                            .extracting(Post::getTitle)
                            .containsExactly("Test Post 1", "Test Post 2", "Test Post 3", "Test Post 4", "Test Post 5");
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
                    assertThat(pagedResult.pageNumber()).isEqualTo(2);
                    assertThat(pagedResult.totalPages()).isEqualTo(3);
                    assertThat(pagedResult.isFirst()).isFalse();
                    assertThat(pagedResult.isLast()).isFalse();
                    assertThat(pagedResult.hasNext()).isTrue();
                    assertThat(pagedResult.hasPrevious()).isTrue();
                    List<Post> posts = pagedResult.data();
                    assertThat(posts).isNotEmpty().hasSize(8);
                    // Verify content
                    assertThat(posts)
                            .extracting(Post::getTitle)
                            .containsExactly(
                                    "Test Post 9",
                                    "Test Post 10",
                                    "Test Post 11",
                                    "Test Post 12",
                                    "Test Post 13",
                                    "Test Post 14",
                                    "Test Post 15",
                                    "Test Post 16");
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
                    assertThat(pagedResult.pageNumber()).isEqualTo(11);
                    assertThat(pagedResult.totalPages()).isEqualTo(2);
                    assertThat(pagedResult.isFirst()).isFalse();
                    assertThat(pagedResult.isLast()).isTrue();
                    assertThat(pagedResult.hasNext()).isFalse();
                    assertThat(pagedResult.hasPrevious()).isTrue();
                    List<Post> posts = pagedResult.data();
                    assertThat(posts).isNull();
                });
    }

    @Test
    void shouldHandleInvalidPageParameters() {
        // Test negative page
        this.webTestClient
                .get()
                .uri("/posts?page=-1&size=10")
                .exchange()
                .expectStatus()
                .isBadRequest();

        // Test negative size
        this.webTestClient
                .get()
                .uri("/posts?page=0&size=-1")
                .exchange()
                .expectStatus()
                .isBadRequest();

        // Test zero size
        this.webTestClient
                .get()
                .uri("/posts?page=0&size=0")
                .exchange()
                .expectStatus()
                .isBadRequest();

        // Test non-numeric values
        this.webTestClient
                .get()
                .uri("/posts?page=abc&size=def")
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    void shouldSortPostsByTitleAscending() {
        this.webTestClient
                .get()
                .uri("/posts?sort=title,asc")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PagedResult<Post>>() {})
                .value(pagedResult -> {
                    List<Post> posts = pagedResult.data();
                    assertThat(posts).extracting(Post::getTitle).isSortedAccordingTo((a, b) -> {
                        // Extract numeric suffixes and compare
                        int numA = Integer.parseInt(a.substring(a.lastIndexOf(" ") + 1));
                        int numB = Integer.parseInt(b.substring(b.lastIndexOf(" ") + 1));
                        return Integer.compare(numA, numB);
                    });
                });
    }

    @Test
    void shouldHandleMaxPageSize() {
        // Add a test for the maximum allowed page size
        this.webTestClient
                .get()
                .uri("/posts?size=50") // Use your actual max page size
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<PagedResult<Post>>() {})
                .value(pagedResult -> {
                    assertThat(pagedResult.data()).hasSizeLessThanOrEqualTo(50);
                });
    }
}
