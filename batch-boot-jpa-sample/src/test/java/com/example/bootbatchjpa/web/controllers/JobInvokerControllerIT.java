package com.example.bootbatchjpa.web.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bootbatchjpa.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class JobInvokerControllerIT extends AbstractIntegrationTest {

    @Test
    void shouldInvokeAllCustomersJob() throws Exception {
        mockMvc.perform(get("/api/job/customers").param("minId", "0").param("maxId", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Batch job has been invoked as")));
    }
}
