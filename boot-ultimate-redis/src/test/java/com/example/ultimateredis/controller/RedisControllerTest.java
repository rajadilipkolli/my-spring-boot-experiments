package com.example.ultimateredis.controller;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ultimateredis.common.AbstractIntegrationTest;
import com.example.ultimateredis.model.AddRedisRequest;
import java.time.Duration;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RedisControllerTest extends AbstractIntegrationTest {

    @Test
    @Order(1)
    void addRedisKeyValue() throws Exception {
        AddRedisRequest addRedisRequest = new AddRedisRequest("junit", "JunitValue", 1);
        this.mockMvc
                .perform(
                        post("/v1/redis/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(addRedisRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.response", is(Boolean.TRUE)));
    }

    @Test
    @Order(2)
    void getFromCache() throws Exception {
        this.mockMvc
                .perform(get("/v1/redis").param("key", "junit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", is("JunitValue")));
    }

    @Test
    @Order(3)
    void expireFromCache() {
        await().pollDelay(Duration.ofMinutes(1))
                .pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(70))
                .untilAsserted(
                        () ->
                                this.mockMvc
                                        .perform(get("/v1/redis").param("key", "junit"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.response", nullValue())));
    }
}
