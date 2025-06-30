package com.example.multitenancy.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.multitenancy.common.AbstractIntegrationTest;
import com.example.multitenancy.primary.entities.PrimaryCustomer;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@DisplayName("Primary Customer Controller Integration Tests")
class PrimaryCustomerControllerIT extends AbstractIntegrationTest {

    private List<PrimaryCustomer> primaryCustomerList = null;

    @BeforeEach
    void setUp() {
        tenantIdentifierResolver.setCurrentTenant("primary");
        primaryCustomerRepository.deleteAllInBatch();

        primaryCustomerList = new ArrayList<>();
        primaryCustomerList.add(new PrimaryCustomer("First Customer"));
        primaryCustomerList.add(new PrimaryCustomer("Second Customer"));
        primaryCustomerList.add(new PrimaryCustomer("Third Customer"));
        primaryCustomerList = primaryCustomerRepository.saveAll(primaryCustomerList);
    }

    @Test
    void shouldFailWhenHeaderNotSetForFetchAllCustomers() throws Exception {
        this.mockMvc
                .perform(get("/api/customers/primary"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Bad Request")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Required header 'X-tenantId' is not present.")))
                .andExpect(jsonPath("$.instance", is("/api/customers/primary")));
    }

    @Test
    void shouldFailWhenWrongHeaderSetForFetchAllCustomers() throws Exception {
        this.mockMvc
                .perform(get("/api/customers/primary").header("X-tenantId", "junk"))
                .andExpect(status().isForbidden())
                .andExpect(header().string("Content-Type", is("application/json")))
                .andExpect(jsonPath("$.error", is("Unknown Database tenant")));
    }

    @Test
    void shouldFetchAllCustomers() throws Exception {
        this.mockMvc
                .perform(get("/api/customers/primary").header("X-tenantId", "primary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(primaryCustomerList.size())));
    }

    @Test
    void shouldFindCustomerById() throws Exception {
        PrimaryCustomer primaryCustomer = primaryCustomerList.getFirst();
        Long customerId = primaryCustomer.getId();

        this.mockMvc
                .perform(
                        get("/api/customers/primary/{id}", customerId)
                                .header("X-tenantId", "primary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(primaryCustomer.getText())))
                .andExpect(jsonPath("$.tenant", is("primary")));
    }

    @Test
    void shouldCreateNewCustomer() throws Exception {
        PrimaryCustomer primaryCustomer = new PrimaryCustomer("New Customer");
        this.mockMvc
                .perform(
                        post("/api/customers/primary")
                                .header("X-tenantId", "primary")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(primaryCustomer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text", is(primaryCustomer.getText())))
                .andExpect(jsonPath("$.tenant", is("primary")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void shouldReturn400WhenCreateNewCustomerWithoutText() throws Exception {
        PrimaryCustomer primaryCustomer = new PrimaryCustomer(null);

        this.mockMvc
                .perform(
                        post("/api/customers/primary")
                                .header("X-tenantId", "primary")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(primaryCustomer)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/customers/primary")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be blank")))
                .andReturn();
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        PrimaryCustomer primaryCustomer = primaryCustomerList.getFirst();
        primaryCustomer.setText("Updated Customer");

        this.mockMvc
                .perform(
                        put("/api/customers/primary/{id}", primaryCustomer.getId())
                                .header("X-tenantId", "primary")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(primaryCustomer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(primaryCustomer.getText())))
                .andExpect(jsonPath("$.tenant", is("primary")));
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        PrimaryCustomer primaryCustomer = primaryCustomerList.getFirst();

        this.mockMvc
                .perform(
                        delete("/api/customers/primary/{id}", primaryCustomer.getId())
                                .header("X-tenantId", "primary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(primaryCustomer.getText())))
                .andExpect(jsonPath("$.tenant", is("primary")));
    }
}
