package com.example.learning;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.learning.common.AbstractIntegrationTest;
import com.example.learning.entity.Post;
import org.junit.jupiter.api.Test;

class ApplicationIT extends AbstractIntegrationTest {

    @Test
    void willLoadPosts() {
        this.webTestClient
                .get()
                .uri("/posts")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Post.class)
                .hasSize(2)
                .value(posts -> {
                    Post post1 = posts.get(0);
                    Post post2 = posts.get(1);
                    assertThat(post1.getTitle()).isNotNull();
                    assertThat(post1.getMetadata()).isNotNull();
                    assertThat(post2.getTitle()).isNotNull();
                    assertThat(post2.getMetadata()).isNotNull();
                });
    }
}
