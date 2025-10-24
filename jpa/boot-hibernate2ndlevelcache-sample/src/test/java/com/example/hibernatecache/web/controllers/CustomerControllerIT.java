package com.example.hibernatecache.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.empty;
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
import com.example.hibernatecache.entities.Order;
import com.example.hibernatecache.entities.OrderItem;
import com.example.hibernatecache.model.request.CustomerRequest;
import io.hypersistence.utils.jdbc.validator.SQLStatementCountValidator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class CustomerControllerIT extends AbstractIntegrationTest {

    private List<Customer> customerList = null;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAllInBatch();
        customerRepository.deleteAllInBatch();

        customerList = new ArrayList<>();
        customerList.add(new Customer()
                .setFirstName("firstName 1")
                .setLastName("lastName 1")
                .setEmail("email1@junit.com")
                .setPhone("9876543211")
                .addOrder(new Order()
                        .setName("First Order")
                        .setPrice(BigDecimal.TEN)
                        .addOrderItem(new OrderItem()
                                .setItemCode("ITM001")
                                .setPrice(BigDecimal.TEN)
                                .setQuantity(4))
                        .addOrderItem(new OrderItem()
                                .setItemCode("ITM002")
                                .setPrice(BigDecimal.TWO)
                                .setQuantity(2))
                        .addOrderItem(new OrderItem()
                                .setItemCode("ITM003")
                                .setPrice(BigDecimal.ONE)
                                .setQuantity(1))));
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
        customerList = customerRepository.persistAllAndFlush(customerList);

        SQLStatementCountValidator.reset();
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

        SQLStatementCountValidator.assertSelectCount(5);
        SQLStatementCountValidator.assertTotalCount(5);
    }

    @Test
    void shouldFindCustomerById() throws Exception {
        Customer customer = customerList.getFirst();
        Long customerId = customer.getId();

        this.mockMvc
                .perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.customerId", is(customerId), Long.class))
                .andExpect(jsonPath("$.firstName", is(customer.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(customer.getLastName())))
                .andExpect(jsonPath("$.email", is(customer.getEmail())))
                .andExpect(jsonPath("$.phone", is(customer.getPhone())))
                .andExpect(jsonPath("$.orders.size()", is(1)));

        SQLStatementCountValidator.assertInsertCount(0);
        // For selecting customer and order
        SQLStatementCountValidator.assertSelectCount(2);
        SQLStatementCountValidator.assertTotalCount(2);
    }

    @Test
    void shouldFindCustomerByFirstname() throws Exception {
        Customer customer = customerList.getFirst();
        String customerFirstName = customer.getFirstName();

        this.mockMvc
                .perform(get("/api/customers/search?firstName=" + customerFirstName))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.customerId", is(customer.getId()), Long.class))
                .andExpect(jsonPath("$.firstName", is(customer.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(customer.getLastName())))
                .andExpect(jsonPath("$.email", is(customer.getEmail())))
                .andExpect(jsonPath("$.phone", is(customer.getPhone())))
                .andExpect(jsonPath("$.orders.size()", is(1)));

        SQLStatementCountValidator.assertInsertCount(0);
        // For selecting customer and then orderItems
        SQLStatementCountValidator.assertSelectCount(2);
        SQLStatementCountValidator.assertTotalCount(2);
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
                .andExpect(jsonPath("$.phone", is(customerRequest.phone())))
                .andExpect(jsonPath("$.orders", empty()));

        SQLStatementCountValidator.assertInsertCount(1);
        SQLStatementCountValidator.assertTotalCount(1);
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
                .andExpect(
                        jsonPath("$.type", is("https://api.boot-hibernate2ndlevelcache-sample.com/errors/validation")))
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
                .andExpect(jsonPath("$.phone", is(customerRequest.phone())))
                .andExpect(jsonPath("$.orders.size()", is(1)));

        SQLStatementCountValidator.assertUpdateCount(1);
        SQLStatementCountValidator.assertSelectCount(2);
        SQLStatementCountValidator.assertTotalCount(3);
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        Customer customer = customerList.getFirst();

        this.mockMvc
                .perform(delete("/api/customers/{id}", customer.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.customerId", is(customer.getId()), Long.class))
                .andExpect(jsonPath("$.firstName", is(customer.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(customer.getLastName())))
                .andExpect(jsonPath("$.email", is(customer.getEmail())))
                .andExpect(jsonPath("$.phone", is(customer.getPhone())))
                .andExpect(jsonPath("$.orders.size()", is(1)));

        // Customer, order and OrderItem
        SQLStatementCountValidator.assertDeleteCount(3);
        SQLStatementCountValidator.assertSelectCount(2);
        SQLStatementCountValidator.assertTotalCount(5);
    }
}
