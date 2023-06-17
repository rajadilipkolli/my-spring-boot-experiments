package com.example.multitenancy.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.multitenancy.common.AbstractIntegrationTest;
import com.example.multitenancy.config.multitenant.TenantIdentifierResolver;
import com.example.multitenancy.secondary.entities.SecondaryCustomer;
import com.example.multitenancy.secondary.repositories.SecondaryCustomerRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class SecondaryCustomerControllerIT extends AbstractIntegrationTest {

    @Autowired private SecondaryCustomerRepository secondaryCustomerRepository;
    @Autowired private TenantIdentifierResolver tenantIdentifierResolver;

    private List<SecondaryCustomer> secondaryCustomerList = null;

    @BeforeEach
    void setUp() {
        tenantIdentifierResolver.setCurrentTenant("test1");
        secondaryCustomerRepository.deleteAllInBatch();

        secondaryCustomerList = new ArrayList<>();
        secondaryCustomerList.add(new SecondaryCustomer(null, "First Customer"));
        secondaryCustomerList.add(new SecondaryCustomer(null, "Second Customer"));
        secondaryCustomerList.add(new SecondaryCustomer(null, "Third Customer"));
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
                .perform(get("/api/customers/secondary").header("X-tenantId", "test1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(secondaryCustomerList.size())));
    }

    @Test
    void shouldFindCustomerById() throws Exception {
        SecondaryCustomer secondaryCustomer = secondaryCustomerList.get(0);
        Long customerId = secondaryCustomer.getId();

        this.mockMvc
                .perform(
                        get("/api/customers/secondary/{id}", customerId)
                                .header("X-tenantId", "test1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(secondaryCustomer.getName())));
    }

    @Test
    void shouldCreateNewCustomer() throws Exception {
        SecondaryCustomer newCustomer = new SecondaryCustomer(null, "New Customer");
        this.mockMvc
                .perform(
                        post("/api/customers/secondary")
                                .header("X-tenantId", "test1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(newCustomer.getName())))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void shouldReturn400WhenCreateNewCustomerWithoutName() throws Exception {
        SecondaryCustomer secondaryCustomer = new SecondaryCustomer(null, null);

        this.mockMvc
                .perform(
                        post("/api/customers/secondary")
                                .header("X-tenantId", "test1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(secondaryCustomer)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Bad Request")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/customers/secondary")))
                .andReturn();
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        SecondaryCustomer secondaryCustomer = secondaryCustomerList.get(0);
        secondaryCustomer.setName("Updated Customer");

        this.mockMvc
                .perform(
                        put("/api/customers/secondary/{id}", secondaryCustomer.getId())
                                .header("X-tenantId", "test1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(secondaryCustomer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(secondaryCustomer.getName())));
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        SecondaryCustomer secondaryCustomer = secondaryCustomerList.get(0);

        this.mockMvc
                .perform(
                        delete("/api/customers/secondary/{id}", secondaryCustomer.getId())
                                .header("X-tenantId", "test1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(secondaryCustomer.getName())));
    }
}
