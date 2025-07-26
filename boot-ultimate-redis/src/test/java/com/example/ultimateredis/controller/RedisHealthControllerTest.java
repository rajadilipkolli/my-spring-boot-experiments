package com.example.ultimateredis.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ultimateredis.common.AbstractIntegrationTest;
import com.example.ultimateredis.model.GenericResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class RedisHealthControllerTest extends AbstractIntegrationTest {

    @Test
    void checkHealth() {
        this.mockMvcTester
                .get()
                .uri("/v1/redis/health")
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(GenericResponse.class)
                .satisfies(response -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> healthStatus = (Map<String, Object>) response.response();
                    assertThat(healthStatus).containsKey("status");
                    assertThat(healthStatus).containsKey("timestamp");

                    // In test environment with Redis running, status should be UP
                    assertThat(healthStatus.get("status")).isEqualTo("UP");

                    // Should have connection info
                    assertThat(healthStatus).containsKey("connectionInfo");
                });
    }
}
