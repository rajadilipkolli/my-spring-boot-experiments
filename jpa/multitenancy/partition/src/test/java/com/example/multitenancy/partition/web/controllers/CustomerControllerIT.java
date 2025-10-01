package com.example.multitenancy.partition.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.multitenancy.partition.common.AbstractIntegrationTest;
import com.example.multitenancy.partition.config.tenant.TenantIdentifierResolver;
import com.example.multitenancy.partition.dto.CustomerDTO;
import com.example.multitenancy.partition.entities.Customer;
import com.example.multitenancy.partition.repositories.CustomerRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class CustomerControllerIT extends AbstractIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TenantIdentifierResolver tenantIdentifierResolver;

    private List<Customer> customerList = null;

    @BeforeEach
    void setUp() {
        tenantIdentifierResolver.setCurrentTenant("dbsystc");
        customerRepository.deleteAll();

        customerList = new ArrayList<>();
        customerList.add(new Customer().setText("First Customer"));
        customerList.add(new Customer().setText("Second Customer"));
        customerList.add(new Customer().setText("Third Customer"));
        customerList = customerRepository.saveAll(customerList);
    }

    @Test
    void shouldFetchAllCustomers() throws Exception {
        this.mockMvc
                .perform(get("/api/customers").param("tenant", "dbsystc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(customerList.size())));
        this.mockMvc
                .perform(get("/api/customers").param("tenant", "dbsystp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(0)));
    }

    @Test
    void shouldFindCustomerById() throws Exception {
        Customer customer = customerList.getFirst();
        Long customerId = customer.getId();

        this.mockMvc
                .perform(get("/api/customers/{id}", customerId).param("tenant", "dbsystc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(customer.getText())));
    }

    @Test
    void shouldCreateNewCustomer() throws Exception {
        CustomerDTO customerDTO = new CustomerDTO("New Customer");
        tenantIdentifierResolver.setCurrentTenant("dbsystc");
        long count = this.customerRepository.countByTenant("dbsystc");
        this.mockMvc
                .perform(post("/api/customers")
                        .param("tenant", "dbsystc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(customerDTO.text())))
                .andExpect(jsonPath("$.tenant", is("dbsystc")));
        tenantIdentifierResolver.setCurrentTenant("dbsystc");
        assertThat(this.customerRepository.countByTenant("dbsystc")).isEqualTo(count + 1);
    }

    @Test
    void shouldReturn400WhenCreateNewCustomerWithoutText() throws Exception {
        CustomerDTO customerDTO = new CustomerDTO(null);

        this.mockMvc
                .perform(post("/api/customers")
                        .param("tenant", "dbsystc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://multitenancy.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/customers")))
                .andReturn();
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        Customer customer = customerList.getFirst();
        customer.setText("Updated Customer");

        this.mockMvc
                .perform(put("/api/customers/{id}", customer.getId())
                        .param("tenant", "dbsystc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(customer.getText())));
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        Customer customer = customerList.getFirst();
        tenantIdentifierResolver.setCurrentTenant("dbsystc");
        long count = this.customerRepository.countByTenant("dbsystc");
        this.mockMvc
                .perform(delete("/api/customers/{id}", customer.getId()).param("tenant", "dbsystc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(customer.getText())));
        tenantIdentifierResolver.setCurrentTenant("dbsystc");
        assertThat(this.customerRepository.countByTenant("dbsystc")).isEqualTo(count - 1);
    }
}
