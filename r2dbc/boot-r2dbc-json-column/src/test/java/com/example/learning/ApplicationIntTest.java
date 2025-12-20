package com.example.learning;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.learning.common.AbstractIntegrationTest;
import com.example.learning.entity.Post;
import com.example.learning.model.response.PagedResult;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

class ApplicationIntTest extends AbstractIntegrationTest {

    @Test
    void willLoadPosts() {
        this.webTestClient
                .get()
                .uri("/posts")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(new ParameterizedTypeReference<PagedResult<Post>>() {})
                .value(pagedResult -> {
                    assertThat(pagedResult.totalElements()).isEqualTo(2);
                    assertThat(pagedResult.pageNumber()).isOne();
                    assertThat(pagedResult.totalPages()).isOne();
                    assertThat(pagedResult.isFirst()).isTrue();
                    assertThat(pagedResult.isLast()).isTrue();
                    assertThat(pagedResult.hasNext()).isFalse();
                    assertThat(pagedResult.hasPrevious()).isFalse();
                    List<Post> posts = pagedResult.data();
                    assertThat(posts).isNotEmpty().hasSize(2);
                    Post post1 = posts.get(0);
                    Post post2 = posts.get(1);
                    assertThat(post1.getTitle()).isNotNull();
                    assertThat(post1.getMetadata()).isNotNull();
                    assertThat(post1.getComments()).isNotEmpty().hasSize(4);
                    assertThat(post2.getTitle()).isNotNull();
                    assertThat(post2.getMetadata()).isNotNull();
                    assertThat(post2.getComments()).isEmpty();
                });
    }

    @Test
    void healthEndpointShouldReturnUp() {
        webTestClient
                .get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("UP");
    }

    @Test
    void healthEndpointShouldShowDetails() {
        webTestClient
                .get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.components")
                .exists();
    }

    @Test
    void metricsEndpointShouldBeAccessible() {
        webTestClient
                .get()
                .uri("/actuator/metrics")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.names")
                .isArray();
    }
}
