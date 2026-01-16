package com.example.highrps.web.controller;

import com.example.highrps.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class PostControllerIT extends AbstractIntegrationTest {

    @Test
    void createPost() {
        mockMvcTester
                .post()
                .content("""
                        {
                          "title": "High RPS with Spring Boot",
                          "content": "This is a sample post content.",
                          "email": "junit@email.com"
                        }
                        """)
                .uri("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasContentType(MediaType.APPLICATION_JSON)
                .containsHeader("Location");
    }
}
