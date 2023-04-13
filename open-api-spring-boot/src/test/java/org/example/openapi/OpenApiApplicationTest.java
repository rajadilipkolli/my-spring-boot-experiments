package org.example.openapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiApplicationTest {

    @Autowired protected MockMvc mockMvc;

    @Test
    public void whenReadAll_thenStatusIsNotImplemented() throws Exception {
        this.mockMvc.perform(get("/api/customers"))
            .andExpect(status().isNotImplemented());
    }

    @Test
    public void whenReadCustomerById_thenStatusIsNotImplemented() throws Exception {
        this.mockMvc.perform(get("/api/customers/1"))
            .andExpect(status().isNotImplemented());
    }

}