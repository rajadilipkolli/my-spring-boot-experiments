package com.example.bootbatchjpa.web.controllers;

import com.example.bootbatchjpa.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class JobInvokerControllerIT extends AbstractIntegrationTest {

    @Test
    void shouldInvokeAllCustomersJob() {
        mockMvcTester
                .get()
                .uri("/api/job/customers")
                .param("minId", "0")
                .param("maxId", "100")
                .assertThat()
                .hasStatusOk()
                .hasContentType("text/plain;charset=UTF-8")
                .hasBodyTextEqualTo("Batch job has been invoked as 2");
    }

    @Test
    void shouldReturnBadRequestForMissingParameters() {
        mockMvcTester.get().uri("/api/job/customers").assertThat().hasStatus(HttpStatus.BAD_REQUEST);
    }
}
