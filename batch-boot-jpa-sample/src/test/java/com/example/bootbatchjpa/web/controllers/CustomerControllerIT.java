package com.example.bootbatchjpa.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.instancio.Select.field;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bootbatchjpa.common.AbstractIntegrationTest;
import com.example.bootbatchjpa.entities.Customer;
import com.example.bootbatchjpa.repositories.CustomerRepository;
import java.util.List;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class CustomerControllerIT extends AbstractIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    private List<Customer> customerList = null;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAllInBatch();

        customerList = Instancio.ofList(Customer.class)
                .size(3)
                .ignore(field(Customer.class, "id"))
                .generate(field(Customer.class, "gender"), gen -> gen.oneOf("male", "female"))
                .create();

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
        Customer customer = customerList.getFirst();
        Long customerId = customer.getId();

        this.mockMvc
                .perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(customer.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(customer.getName())));
    }

    @Test
    void shouldCreateNewCustomer() throws Exception {
        Customer customer = Instancio.create(Customer.class);
        customer.setId(null);
        this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(customer.getName())));
    }

    @Test
    void shouldReturn400WhenCreateNewCustomerWithoutName() throws Exception {
        Customer customer = new Customer(null, null, null, null);

        this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(customer)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://boot-jpa.com/errors/validation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/customers")))
                .andExpect(jsonPath("$.properties.violations", hasSize(1)))
                .andExpect(jsonPath("$.properties.violations[0].field", is("name")))
                .andExpect(jsonPath("$.properties.violations[0].message", is("Name cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        Customer customer = customerList.getFirst();
        customer.setName("Updated Customer");

        this.mockMvc
                .perform(put("/api/customers/{id}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(customer.getId()), Long.class))
                .andExpect(jsonPath("$.name", is("Updated Customer")));
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        Customer customer = customerList.getFirst();

        this.mockMvc
                .perform(delete("/api/customers/{id}", customer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(customer.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(customer.getName())));
    }
}
