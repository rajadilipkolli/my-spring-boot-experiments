package com.example.multitenancy.schema.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.multitenancy.schema.common.AbstractIntegrationTest;
import com.example.multitenancy.schema.config.TenantIdentifierResolver;
import com.example.multitenancy.schema.entities.Customer;
import com.example.multitenancy.schema.repositories.CustomerRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class CustomerControllerIT extends AbstractIntegrationTest {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private TenantIdentifierResolver tenantIdentifierResolver;

    private List<Customer> customerList = null;

    @BeforeEach
    void setUp() {
        tenantIdentifierResolver.setCurrentTenant("test1");
        customerRepository.deleteAll();

        customerList = new ArrayList<>();
        customerList.add(new Customer(null, "First Customer"));
        customerList.add(new Customer(null, "Second Customer"));
        customerList.add(new Customer(null, "Third Customer"));
        customerList = customerRepository.saveAll(customerList);
    }

    @Test
    void shouldFetchAllCustomers() throws Exception {
        this.mockMvc
                .perform(get("/api/customers").param("tenant", "test1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(customerList.size())));
        this.mockMvc
                .perform(get("/api/customers").param("tenant", "test2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(0)));
    }

    @Test
    void shouldFindCustomerById() throws Exception {
        Customer customer = customerList.get(0);
        Long customerId = customer.getId();

        this.mockMvc
                .perform(get("/api/customers/{id}", customerId).param("tenant", "test1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(customer.getName())));

        this.mockMvc
                .perform(get("/api/customers/{id}", customerId).param("tenant", "test2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewCustomer() throws Exception {
        Customer customer = new Customer(null, "New Customer");
        this.mockMvc
                .perform(
                        post("/api/customers")
                                .param("tenant", "test1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(customer.getName())));
    }

    @Test
    void shouldReturn400WhenCreateNewCustomerWithoutName() throws Exception {
        Customer customer = new Customer(null, null);

        this.mockMvc
                .perform(
                        post("/api/customers")
                                .param("tenant", "test1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("name")))
                .andExpect(jsonPath("$.violations[0].message", is("Name cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        Customer customer = customerList.get(0);
        customer.setName("Updated Customer");

        this.mockMvc
                .perform(
                        put("/api/customers/{id}", customer.getId())
                                .param("tenant", "test1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(customer.getName())));

        this.mockMvc
                .perform(
                        put("/api/customers/{id}", customer.getId())
                                .param("tenant", "test2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        Customer customer = customerList.get(0);

        this.mockMvc
                .perform(delete("/api/customers/{id}", customer.getId()).param("tenant", "test1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(customer.getName())));

        this.mockMvc
                .perform(delete("/api/customers/{id}", customer.getId()).param("tenant", "test1"))
                .andExpect(status().isNotFound());
    }
}
