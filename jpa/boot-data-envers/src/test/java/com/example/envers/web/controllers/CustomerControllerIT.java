package com.example.envers.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.envers.common.AbstractIntegrationTest;
import com.example.envers.entities.Customer;
import com.example.envers.model.request.CustomerRequest;
import com.example.envers.repositories.CustomerRepository;
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

    private List<Customer> customerList = null;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAllInBatch();

        customerList = new ArrayList<>();
        customerList.add(new Customer(null, "First Customer", "Junit Address", 0L));
        customerList.add(new Customer(null, "Second Customer", "Junit Address", 0L));
        customerList.add(new Customer(null, "Third Customer", "Junit Address", 0L));
        customerList = customerRepository.saveAll(customerList);
    }

    @Test
    void shouldFetchAllCustomers() throws Exception {
        this.mockMvc
                .perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(customerList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindCustomerById() throws Exception {
        Customer customer = customerList.get(0);
        Long customerId = customer.getId();

        this.mockMvc
                .perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(customer.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(customer.getName())))
                .andExpect(jsonPath("$.address", is(customer.getAddress())));
    }

    @Test
    void shouldFindCustomerRevisionsById() throws Exception {
        Customer customer = customerList.get(0);
        Long customerId = customer.getId();

        this.mockMvc
                .perform(get("/api/customers/{id}/revisions", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].entity.id", is(customer.getId()), Long.class))
                .andExpect(jsonPath("$[0].entity.name", is(customer.getName())))
                .andExpect(jsonPath("$[0].entity.address", is(customer.getAddress())))
                .andExpect(jsonPath("$[0].revisionNumber", notNullValue()))
                .andExpect(jsonPath("$[0].revisionType", is("INSERT")));
    }

    @Test
    void shouldCreateNewCustomer() throws Exception {
        CustomerRequest customerRequest = new CustomerRequest("New Customer", "Junit Address");
        this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(customerRequest.name())))
                .andExpect(jsonPath("$.address", is(customerRequest.address())));
    }

    @Test
    void shouldReturn400WhenCreateNewCustomerWithoutName() throws Exception {
        CustomerRequest customerRequest = new CustomerRequest(null, null);

        this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/customers")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("name")))
                .andExpect(jsonPath("$.violations[0].message", is("Name cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        Long customerId = customerList.get(0).getId();
        CustomerRequest customerRequest = new CustomerRequest("Updated Customer", "Junit Address");

        this.mockMvc
                .perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(customerId), Long.class))
                .andExpect(jsonPath("$.name", is(customerRequest.name())))
                .andExpect(jsonPath("$.address", is("Junit Address")));
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        Customer customer = customerList.get(0);

        this.mockMvc
                .perform(delete("/api/customers/{id}", customer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(customer.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(customer.getName())))
                .andExpect(jsonPath("$.address", is("Junit Address")));
    }
}
