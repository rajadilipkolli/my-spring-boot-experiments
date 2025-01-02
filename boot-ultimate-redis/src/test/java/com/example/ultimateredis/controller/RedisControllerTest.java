package com.example.ultimateredis.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.ultimateredis.common.AbstractIntegrationTest;
import com.example.ultimateredis.model.AddRedisRequest;
import com.example.ultimateredis.model.GenericResponse;
import java.time.Duration;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RedisControllerTest extends AbstractIntegrationTest {

    @Test
    @Order(1)
    void addRedisKeyValue() throws Exception {
        AddRedisRequest addRedisRequest = new AddRedisRequest("junit", "JunitValue", 1);
        this.mockMvcTester
                .post()
                .uri("/v1/redis/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addRedisRequest))
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(GenericResponse.class)
                .satisfies(response -> assertThat(response.response()).isEqualTo(true));
    }

    @Test
    @Order(2)
    void getFromCache() {
        this.mockMvcTester
                .get()
                .uri("/v1/redis")
                .param("key", "junit")
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(GenericResponse.class)
                .satisfies(response -> assertThat(response.response()).isEqualTo("JunitValue"));
    }

    @Test
    @Order(3)
    void expireFromCache() {
        await().pollDelay(Duration.ofMinutes(1))
                .pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(70))
                .untilAsserted(
                        () ->
                                this.mockMvcTester
                                        .get()
                                        .uri("/v1/redis")
                                        .param("key", "junit")
                                        .assertThat()
                                        .hasStatusOk()
                                        .hasContentType(MediaType.APPLICATION_JSON)
                                        .bodyJson()
                                        .convertTo(GenericResponse.class)
                                        .satisfies(
                                                response ->
                                                        assertThat(response.response()).isNull()));
    }
}
