package com.example.jooq.r2dbc.router;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jooq.r2dbc.common.AbstractIntegrationTest;
import com.example.jooq.r2dbc.model.response.PaginatedResult;
import org.junit.jupiter.api.Test;

class TagRouterIT extends AbstractIntegrationTest {

    @Test
    void findAllTags() {
        this.webTestClient
                .get()
                .uri("/tags")
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
}
