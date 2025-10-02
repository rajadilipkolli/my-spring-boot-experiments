package com.example.multitenancy.db.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.multitenancy.db.common.AbstractIntegrationTest;
import com.example.multitenancy.db.config.multitenant.TenantIdentifierResolver;
import com.example.multitenancy.db.entities.Customer;
import com.example.multitenancy.db.repositories.CustomerRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class CustomerControllerIT extends AbstractIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TenantIdentifierResolver tenantIdentifierResolver;

    private List<Customer> customerList = null;
    private List<Customer> secondaryCustomers = null;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();

        // Set tenant to primary and save customers
        tenantIdentifierResolver.setCurrentTenant("primary");
        customerList = new ArrayList<>();
        customerList.add(new Customer().setText("First Customer"));
        customerList.add(new Customer().setText("Second Customer"));
        customerRepository.saveAll(customerList);

        // Set tenant to secondary and save customers
        tenantIdentifierResolver.setCurrentTenant("secondary");
        secondaryCustomers = new ArrayList<>();
        secondaryCustomers.add(new Customer().setText("First Customer"));
        secondaryCustomers.add(new Customer().setText("Second Customer"));
        secondaryCustomers.add(new Customer().setText("Third Customer"));
        customerRepository.saveAll(secondaryCustomers);

        // Reset tenant to unknown
        tenantIdentifierResolver.setCurrentTenant(null);
    }

    @Test
    void shouldNotFetchAllCustomersWhenHeaderNotSet() throws Exception {
        this.mockMvc
                .perform(get("/api/customers"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://multitenancy.com/errors/header-error")))
                .andExpect(jsonPath("$.title", is("Header Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Required header 'X-tenantId' is not present.")))
                .andExpect(jsonPath("$.instance", is("/api/customers")));
    }

    @Test
    void shouldFetchAllCustomersWhenWrongHeaderSet() throws Exception {
        this.mockMvc
                .perform(get("/api/customers").header("X-tenantId", "junk"))
                .andExpect(status().isForbidden())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.error", is("Unknown Database tenant")));
    }

    @Test
    void shouldFetchAllCustomers() throws Exception {
        this.mockMvc
                .perform(get("/api/customers").header("X-tenantId", "primary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(customerList.size())));
    }

    @Test
    void shouldFindCustomerById() throws Exception {
        Customer customer = customerList.getFirst();
        Long customerId = customer.getId();

        this.mockMvc
                .perform(get("/api/customers/{id}", customerId).header("X-tenantId", "primary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(customer.getText())));
    }

    @Test
    void shouldCreateNewCustomer() throws Exception {
        Customer customer = new Customer().setText("New Customer");
        this.mockMvc
                .perform(post("/api/customers")
                        .header("X-tenantId", "primary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text", is(customer.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewCustomerWithoutText() throws Exception {
        Customer customer = new Customer();

        this.mockMvc
                .perform(post("/api/customers")
                        .header("X-tenantId", "primary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
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
                        .header("X-tenantId", "primary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(customer.getText())));
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        Customer customer = customerList.getFirst();

        this.mockMvc
                .perform(delete("/api/customers/{id}", customer.getId()).header("X-tenantId", "primary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(customer.getText())));
    }

    @Nested
    class SecondaryTenantTests {

        @Test
        void shouldFetchAllCustomersForSecondaryTenant() throws Exception {
            mockMvc.perform(get("/api/customers").header("X-tenantId", "secondary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(secondaryCustomers.size())));
        }

        @Test
        void shouldCreateNewCustomerForSecondaryTenant() throws Exception {

            Customer customer = new Customer().setText("New Secondary Customer");

            mockMvc.perform(post("/api/customers")
                            .header("X-tenantId", "secondary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customer)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.text", is(customer.getText())));
        }

        @Test
        void shouldFindCustomerByIdForSecondaryTenant() throws Exception {

            Customer customer = secondaryCustomers.getFirst();
            mockMvc.perform(get("/api/customers/{id}", customer.getId()).header("X-tenantId", "secondary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.text", is(customer.getText())));
        }

        @Test
        void shouldUpdateCustomerForSecondaryTenant() throws Exception {
            Customer customer = secondaryCustomers.getFirst();
            customer.setText("Updated Secondary Customer");

            mockMvc.perform(put("/api/customers/{id}", customer.getId())
                            .header("X-tenantId", "secondary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customer)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.text", is(customer.getText())));
        }

        @Test
        void shouldDeleteCustomerForSecondaryTenant() throws Exception {
            Customer customer = secondaryCustomers.getFirst();

            mockMvc.perform(delete("/api/customers/{id}", customer.getId()).header("X-tenantId", "secondary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.text", is(customer.getText())));
        }

        @Test
        void shouldNotDeleteCustomerForSecondaryTenant() throws Exception {
            Customer customer = customerList.getFirst();

            mockMvc.perform(delete("/api/customers/{id}", customer.getId()).header("X-tenantId", "secondary"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404WhenFetchingNonExistingCustomerForSecondaryTenant() throws Exception {
            mockMvc.perform(get("/api/customers/{id}", 999L).header("X-tenantId", "secondary"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn400WhenCreatingCustomerWithEmptyTextForSecondaryTenant() throws Exception {
            Customer customer = new Customer().setText("");

            mockMvc.perform(post("/api/customers")
                            .header("X-tenantId", "secondary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customer)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                    .andExpect(jsonPath("$.type", is("https://multitenancy.com/errors/validation-error")))
                    .andExpect(jsonPath("$.title", is("Constraint Violation")))
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                    .andExpect(jsonPath("$.instance", is("/api/customers")));
        }
    }
}
