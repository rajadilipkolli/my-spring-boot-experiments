package org.example.openapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.openapitools.OpenApiGeneratorApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;

@SpringBootTest(classes = OpenApiGeneratorApplication.class)
@AutoConfigureMockMvc
class OpenApiApplicationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Test
    void loadingSwagger() throws Exception {
        this.mockMvc.perform(get("/"))
                .andExpect(status().isFound());
    }

    @Test
    void whenReadAll_thenStatusIsNotImplemented() throws Exception {
        this.mockMvc.perform(get("/api/customers"))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void whenReadCustomerById_thenStatusIsNotImplemented() throws Exception {
        this.mockMvc.perform(get("/api/customers/1").header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotImplemented());
    }

}