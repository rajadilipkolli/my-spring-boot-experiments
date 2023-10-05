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
import com.example.hibernatecache.model.request.OrderRequest;
import com.example.hibernatecache.repositories.CustomerRepository;
import com.example.hibernatecache.repositories.OrderRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class OrderControllerIT extends AbstractIntegrationTest {

    @Autowired private OrderRepository orderRepository;
    @Autowired private CustomerRepository customerRepository;

    private List<Order> orderList = null;

    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAllInBatch();
        customerRepository.deleteAll();

        savedCustomer =
                customerRepository.save(
                        new Customer(
                                null,
                                "firstName 1",
                                "lastName 1",
                                "email1@junit.com",
                                "9876543211",
                                null));
        orderList = new ArrayList<>();
        orderList.add(new Order(null, "First Order", BigDecimal.TEN, savedCustomer, null));
        orderList.add(new Order(null, "Second Order", BigDecimal.TEN, savedCustomer, null));
        orderList.add(new Order(null, "Third Order", BigDecimal.TEN, savedCustomer, null));
        orderList = orderRepository.saveAll(orderList);
    }

    @Test
    void shouldFetchAllOrders() throws Exception {
        this.mockMvc
                .perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(orderList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindOrderById() throws Exception {
        Order order = orderList.get(0);
        Long orderId = order.getId();

        this.mockMvc
                .perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(order.getId()), Long.class))
                .andExpect(jsonPath("$.customerId", is(savedCustomer.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(order.getName())))
                .andExpect(jsonPath("$.orderItems", empty()));
    }

    @Test
    void shouldCreateNewOrder() throws Exception {
        OrderRequest orderRequest =
                new OrderRequest(savedCustomer.getId(), "New Order", BigDecimal.TEN);
        this.mockMvc
                .perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId", notNullValue()))
                .andExpect(jsonPath("$.customerId", is(savedCustomer.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(orderRequest.name())))
                .andExpect(jsonPath("$.price", is(orderRequest.price()), BigDecimal.class))
                .andExpect(jsonPath("$.orderItems", empty()));
    }

    @Test
    void shouldReturn400WhenCreateNewOrderWithoutName() throws Exception {
        OrderRequest order = new OrderRequest(null, null, BigDecimal.ZERO);

        this.mockMvc
                .perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/orders")))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("name")))
                .andExpect(jsonPath("$.violations[0].message", is("Name cannot be blank")))
                .andExpect(jsonPath("$.violations[1].field", is("price")))
                .andExpect(
                        jsonPath(
                                "$.violations[1].message",
                                is("Value must be greater than or equal to 0.01")))
                .andReturn();
    }

    @Test
    void shouldUpdateOrder() throws Exception {
        Long customerId = savedCustomer.getId();
        OrderRequest orderRequest = new OrderRequest(customerId, "Updated Order", BigDecimal.TWO);

        Long orderId = orderList.get(0).getId();
        this.mockMvc
                .perform(
                        put("/api/orders/{id}", orderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(orderId), Long.class))
                .andExpect(jsonPath("$.customerId", is(customerId), Long.class))
                .andExpect(jsonPath("$.name", is("Updated Order")))
                .andExpect(jsonPath("$.price", is(orderRequest.price()), BigDecimal.class))
                .andExpect(jsonPath("$.orderItems", empty()));
    }

    @Test
    void shouldDeleteOrder() throws Exception {
        Order order = orderList.get(0);

        this.mockMvc
                .perform(delete("/api/orders/{id}", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(order.getId()), Long.class))
                .andExpect(jsonPath("$.customerId", is(savedCustomer.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(order.getName())))
                .andExpect(jsonPath("$.orderItems", empty()));
    }
}
