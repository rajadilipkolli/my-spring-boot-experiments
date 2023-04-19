package com.example.featuretoggle;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.featuretoggle.common.AbstractIntegrationTest;
import com.example.featuretoggle.entities.Customer;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"togglz.features.ADD_NEW_FIELDS.enabled=true"})
class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldNotFindCustomerById() throws Exception {
        Customer customer = new Customer(101L, "New Customer", "name 1", 1);
        this.mockMvc
                .perform(
                        post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text", is(customer.getText())));

        this.mockMvc
                .perform(get("/api/customers/{id}", customer.getId()))
                .andExpect(status().isNotFound());
    }
}
