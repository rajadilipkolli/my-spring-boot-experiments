package com.example.bootbatchjpa.web.controllers;

import com.example.bootbatchjpa.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class JobInvokerControllerIT extends AbstractIntegrationTest {

    @Test
    void shouldInvokeAllCustomersJob() {
        mockMvcTester
                .get()
                .uri("/api/job/customers")
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON_VALUE)
                .bodyJson()
                .hasPath("message")
                .extractingPath("message")
                .asString()
                .matches("Batch job has been invoked as \\d+");
    }
}
