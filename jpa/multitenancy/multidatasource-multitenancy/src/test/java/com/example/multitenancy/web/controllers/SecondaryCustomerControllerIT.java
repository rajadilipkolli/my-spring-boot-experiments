package com.example.multitenancy.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.multitenancy.common.AbstractIntegrationTest;
import com.example.multitenancy.secondary.entities.SecondaryCustomer;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class SecondaryCustomerControllerIT extends AbstractIntegrationTest {

    private List<SecondaryCustomer> secondaryCustomerList = null;

    @BeforeEach
    void setUp() {
        tenantIdentifierResolver.setCurrentTenant("schema1");
        secondaryCustomerRepository.deleteAllInBatch();

        secondaryCustomerList = new ArrayList<>();
        secondaryCustomerList.add(new SecondaryCustomer().setName("First Customer"));
        secondaryCustomerList.add(new SecondaryCustomer().setName("Second Customer"));
        secondaryCustomerList.add(new SecondaryCustomer().setName("Third Customer"));
        secondaryCustomerList = secondaryCustomerRepository.saveAll(secondaryCustomerList);
    }

    @Test
    void shouldFailWhenHeaderNotSetForFetchAllCustomers() throws Exception {
        this.mockMvc
                .perform(get("/api/customers/secondary"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Bad Request")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Required header 'X-tenantId' is not present.")))
                .andExpect(jsonPath("$.instance", is("/api/customers/secondary")));
    }

    @Test
    void shouldFailWhenWrongHeaderSetForFetchAllCustomers() throws Exception {
        this.mockMvc
                .perform(get("/api/customers/secondary").header("X-tenantId", "junk"))
                .andExpect(status().isForbidden())
                .andExpect(header().string("Content-Type", is("application/json")))
                .andExpect(jsonPath("$.error", is("Unknown Database tenant")));
    }

    @Test
    void shouldFetchAllCustomers() throws Exception {
        this.mockMvc
                .perform(get("/api/customers/secondary").header("X-tenantId", "schema1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(secondaryCustomerList.size())));

        this.mockMvc
                .perform(get("/api/customers/secondary").header("X-tenantId", "schema2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(0)));
    }

    @Test
    void shouldFindCustomerById() throws Exception {
        SecondaryCustomer secondaryCustomer = secondaryCustomerList.getFirst();
        Long customerId = secondaryCustomer.getId();

        this.mockMvc
                .perform(
                        get("/api/customers/secondary/{id}", customerId)
                                .header("X-tenantId", "schema1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(secondaryCustomer.getName())));

        this.mockMvc
                .perform(
                        get("/api/customers/secondary/{id}", customerId)
                                .header("X-tenantId", "schema2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewCustomer() throws Exception {
        SecondaryCustomer newCustomer = new SecondaryCustomer().setName("New Customer");
        this.mockMvc
                .perform(
                        post("/api/customers/secondary")
                                .header("X-tenantId", "schema1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(newCustomer.getName())))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void shouldReturn400WhenCreateNewCustomerWithoutName() throws Exception {
        SecondaryCustomer secondaryCustomer = new SecondaryCustomer().setName(null);

        this.mockMvc
                .perform(
                        post("/api/customers/secondary")
                                .header("X-tenantId", "schema1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(secondaryCustomer)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/customers/secondary")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("name")))
                .andExpect(jsonPath("$.violations[0].message", is("Name cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        SecondaryCustomer secondaryCustomer = secondaryCustomerList.getFirst();
        secondaryCustomer.setName("Updated Customer");

        this.mockMvc
                .perform(
                        put("/api/customers/secondary/{id}", secondaryCustomer.getId())
                                .header("X-tenantId", "schema1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(secondaryCustomer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(secondaryCustomer.getName())));
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        SecondaryCustomer secondaryCustomer = secondaryCustomerList.getFirst();

        this.mockMvc
                .perform(
                        delete("/api/customers/secondary/{id}", secondaryCustomer.getId())
                                .header("X-tenantId", "schema1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(secondaryCustomer.getName())));
    }
}
