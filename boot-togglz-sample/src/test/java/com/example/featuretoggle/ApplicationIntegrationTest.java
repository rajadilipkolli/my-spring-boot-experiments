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

@TestPropertySource(
        properties = {
            "togglz.features.TEXT.enabled=false",
            "togglz.features.NAME.enabled=false",
            "togglz.features.ZIP.enabled=true"
        })
class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldFindCustomerByIdWithEmptyData() throws Exception {
        Customer customer =
                new Customer().setText("New Customer").setName("name 1").setZipCode(12345);
        this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text", is(customer.getText())))
                .andExpect(jsonPath("$.name", is(customer.getName())))
                .andExpect(jsonPath("$.zipCode").value(customer.getZipCode()))
                .andExpect(jsonPath("$.id", is(1)));

        this.mockMvc
                .perform(get("/api/customers/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").doesNotExist())
                .andExpect(jsonPath("$.name").doesNotExist())
                .andExpect(jsonPath("$.zipCode").value(customer.getZipCode()))
                .andExpect(jsonPath("$.id", is(1)));
    }
}
