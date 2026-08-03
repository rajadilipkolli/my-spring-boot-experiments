package com.example.ultimatepostgres.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ultimatepostgres.common.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ControllerIntegrationTest extends AbstractIntegrationTest {

    @BeforeEach
    void setUp() {
        cacheService.evictAll();
        jobQueueRepository.deleteAll();
    }

    @Test
    void testCacheController() {
        // PUT successful
        mockMvcTester
                .put()
                .uri("/api/cache/testKey")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"value": {"data": "val"}, "ttlMillis": 60000}""")
                .assertThat()
                .hasStatusOk();

        assertThat(cacheService.get("testKey").orElseThrow().toString()).contains("\"data\":\"val\"");

        // GET successful
        mockMvcTester
                .get()
                .uri("/api/cache/testKey")
                .assertThat()
                .hasStatusOk()
                .satisfies(result ->
                        assertThat(result.getResponse().getContentAsString()).contains("\"data\":\"val\""));

        // DELETE
        mockMvcTester.delete().uri("/api/cache/testKey").assertThat().hasStatus(204); // NO_CONTENT

        // GET 404
        mockMvcTester.get().uri("/api/cache/testKey").assertThat().hasStatus(404); // NOT_FOUND
    }

    @Test
    void testCacheControllerValidation() {
        // Missing ttlMillis
        mockMvcTester
                .put()
                .uri("/api/cache/badKey")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"value": {"data": "val"}}""")
                .assertThat()
                .hasStatus4xxClientError();
    }

    @Test
    void testJobQueueController() {
        // POST successful
        mockMvcTester
                .post()
                .uri("/api/queue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"payload": {"task": "test"}, "priority": 5}""")
                .assertThat()
                .hasStatusOk()
                .satisfies(result ->
                        assertThat(result.getResponse().getContentAsString()).contains("\"task\":\"test\""));

        // GET status
        mockMvcTester
                .get()
                .uri("/api/queue/status")
                .assertThat()
                .hasStatusOk()
                .satisfies(result ->
                        assertThat(result.getResponse().getContentAsString()).contains("\"task\":\"test\""));
    }

    @Test
    void testJobQueueControllerValidation() {
        // Missing payload
        mockMvcTester
                .post()
                .uri("/api/queue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"priority": 5}""")
                .assertThat()
                .hasStatus4xxClientError();
    }

    @Test
    void testPubSubController() {
        // POST publish
        mockMvcTester
                .post()
                .uri("/api/pubsub/publish")
                .contentType(MediaType.TEXT_PLAIN) // Since it expects a String
                .content("test-message-from-controller")
                .assertThat()
                .hasStatusOk();

        // GET messages
        mockMvcTester.get().uri("/api/pubsub/messages").assertThat().hasStatusOk();
    }
}
