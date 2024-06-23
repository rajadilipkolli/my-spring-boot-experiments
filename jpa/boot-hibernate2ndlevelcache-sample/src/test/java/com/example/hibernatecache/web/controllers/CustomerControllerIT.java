package com.example.hibernatecache.web.controllers;

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

import com.example.hibernatecache.common.AbstractIntegrationTest;
import com.example.hibernatecache.entities.Customer;
import com.example.hibernatecache.model.request.CustomerRequest;
import com.example.hibernatecache.repositories.CustomerRepository;
import com.example.hibernatecache.repositories.OrderRepository;
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
    private OrderRepository orderRepository;

    private List<Customer> customerList = null;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        customerRepository.deleteAll();

        customerList = new ArrayList<>();
        customerList.add(new Customer()
                .setFirstName("firstName 1")
                .setLastName("lastName 1")
                .setEmail("email1@junit.com")
                .setPhone("9876543211"));
        customerList.add(new Customer()
                .setFirstName("firstName 2")
                .setLastName("lastName 2")
                .setEmail("email2@junit.com")
                .setPhone("9876543212"));
        customerList.add(new Customer()
                .setFirstName("firstName 3")
                .setLastName("lastName 3")
                .setEmail("email3@junit.com")
                .setPhone("9876543213"));
        customerList = customerRepository.persistAll(customerList);
    }

    @Test
    void shouldFetchAllCustomers() throws Exception {
        this.mockMvc
                .perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
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
        Customer customer = customerList.getFirst();
        Long customerId = customer.getId();

        this.mockMvc
                .perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.firstName", is(customer.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(customer.getLastName())))
                .andExpect(jsonPath("$.email", is(customer.getEmail())))
                .andExpect(jsonPath("$.phone", is(customer.getPhone())));
    }

    @Test
    void shouldCreateNewCustomer() throws Exception {
        CustomerRequest customerRequest =
                new CustomerRequest("firstName 4", "lastName 4", "email4@junit.com", "9876543213");
        this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.customerId", notNullValue()))
                .andExpect(jsonPath("$.firstName", is(customerRequest.firstName())))
                .andExpect(jsonPath("$.lastName", is(customerRequest.lastName())))
                .andExpect(jsonPath("$.email", is(customerRequest.email())))
                .andExpect(jsonPath("$.phone", is(customerRequest.phone())));
    }

    @Test
    void shouldReturn400WhenCreateNewCustomerWithoutFirstName() throws Exception {
        CustomerRequest customerRequest = new CustomerRequest(null, null, "email", null);

        this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/customers")))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("email")))
                .andExpect(jsonPath("$.violations[0].message", is("Email value must be a well-formed email address")))
                .andExpect(jsonPath("$.violations[1].field", is("firstName")))
                .andExpect(jsonPath("$.violations[1].message", is("FirstName must not be blank")))
                .andReturn();
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        Customer customer = customerList.getFirst();
        CustomerRequest customerRequest = new CustomerRequest(
                "Updated Customer", customer.getLastName(), customer.getEmail(), customer.getPhone());

        this.mockMvc
                .perform(put("/api/customers/{id}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.customerId", is(customer.getId()), Long.class))
                .andExpect(jsonPath("$.firstName", is(customerRequest.firstName())))
                .andExpect(jsonPath("$.lastName", is(customerRequest.lastName())))
                .andExpect(jsonPath("$.email", is(customerRequest.email())))
                .andExpect(jsonPath("$.phone", is(customerRequest.phone())));
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        Customer customer = customerList.getFirst();

        this.mockMvc
                .perform(delete("/api/customers/{id}", customer.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.firstName", is(customer.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(customer.getLastName())))
                .andExpect(jsonPath("$.email", is(customer.getEmail())))
                .andExpect(jsonPath("$.phone", is(customer.getPhone())));
    }
}
