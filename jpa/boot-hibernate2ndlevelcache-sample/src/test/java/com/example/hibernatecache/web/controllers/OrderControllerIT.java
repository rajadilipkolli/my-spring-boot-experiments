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
import com.example.hibernatecache.entities.Order;
import com.example.hibernatecache.entities.OrderItem;
import com.example.hibernatecache.model.request.OrderItemRequest;
import com.example.hibernatecache.model.request.OrderRequest;
import com.example.hibernatecache.repositories.CustomerRepository;
import com.example.hibernatecache.repositories.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class OrderControllerIT extends AbstractIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private List<Order> orderList = null;

    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        customerRepository.deleteAll();

        savedCustomer = customerRepository.persist(new Customer()
                .setFirstName("firstName 1")
                .setLastName("lastName 1")
                .setEmail("email1@junit.com")
                .setPhone("9876543211")
                .addOrder(new Order()
                        .setName("First Order")
                        .setPrice(BigDecimal.valueOf(100))
                        .addOrderItem(new OrderItem().setPrice(BigDecimal.TEN).setQuantity(10)))
                .addOrder(new Order()
                        .setName("Second Order")
                        .setPrice(BigDecimal.valueOf(4))
                        .addOrderItem(new OrderItem().setPrice(BigDecimal.TWO).setQuantity(5)))
                .addOrder(new Order()
                        .setName("Third Order")
                        .setPrice(BigDecimal.valueOf(1))
                        .addOrderItem(new OrderItem().setPrice(BigDecimal.ONE).setQuantity(1))));
        orderList = savedCustomer.getOrders();
    }

    @Test
    void shouldFetchAllOrders() throws Exception {
        this.mockMvc
                .perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
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
        Order order = orderList.getFirst();
        Long orderId = order.getId();

        this.mockMvc
                .perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.orderId", is(order.getId()), Long.class))
                .andExpect(jsonPath("$.customerId", is(savedCustomer.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(order.getName())))
                .andExpect(jsonPath("$.price", is(100)))
                .andExpect(jsonPath("$.orderItems.size()", is(1)))
                .andExpect(jsonPath("$.orderItems[0].price", is(10)))
                .andExpect(jsonPath("$.orderItems[0].quantity", is(10)))
                .andExpect(jsonPath("$.orderItems[0].orderItemId", notNullValue()));
    }

    @Test
    void shouldCreateNewOrder() throws Exception {
        OrderRequest orderRequest = new OrderRequest(
                savedCustomer.getId(),
                "New Order",
                List.of(new OrderItemRequest(BigDecimal.TEN, 10), new OrderItemRequest(BigDecimal.TWO, 2)));
        this.mockMvc
                .perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.orderId", notNullValue()))
                .andExpect(jsonPath("$.customerId", is(savedCustomer.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(orderRequest.name())))
                .andExpect(jsonPath("$.price", is(104)))
                .andExpect(jsonPath("$.orderItems.size()", is(2)))
                .andExpect(jsonPath("$.orderItems[0].price", is(10)))
                .andExpect(jsonPath("$.orderItems[0].quantity", is(10)))
                .andExpect(jsonPath("$.orderItems[0].orderItemId", notNullValue()));
    }

    @Test
    void shouldReturn400WhenCreateNewOrderWithoutName() throws Exception {
        OrderRequest orderRequest = new OrderRequest(null, null, null);

        this.mockMvc
                .perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/orders")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("name")))
                .andExpect(jsonPath("$.violations[0].message", is("Name cannot be blank")))
                .andReturn();
    }

    @Test
    void shouldUpdateOrder() throws Exception {
        Long customerId = savedCustomer.getId();
        OrderRequest orderRequest =
                new OrderRequest(customerId, "Updated Order", List.of(new OrderItemRequest(BigDecimal.TEN, 10)));

        Long orderId = orderList.getFirst().getId();
        this.mockMvc
                .perform(put("/api/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.orderId", is(orderId), Long.class))
                .andExpect(jsonPath("$.customerId", is(customerId), Long.class))
                .andExpect(jsonPath("$.name", is("Updated Order")))
                .andExpect(jsonPath("$.price", is(100)))
                .andExpect(jsonPath("$.orderItems.size()", is(1)))
                .andExpect(jsonPath("$.orderItems[0].price", is(10)))
                .andExpect(jsonPath("$.orderItems[0].quantity", is(10)))
                .andExpect(jsonPath("$.orderItems[0].orderItemId", notNullValue()));
    }

    @Test
    void shouldDeleteOrder() throws Exception {
        Order order = orderList.getFirst();

        this.mockMvc
                .perform(delete("/api/orders/{id}", order.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.orderId", is(order.getId()), Long.class))
                .andExpect(jsonPath("$.customerId", is(savedCustomer.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(order.getName())))
                .andExpect(jsonPath("$.price", is(100)))
                .andExpect(jsonPath("$.orderItems.size()", is(1)))
                .andExpect(jsonPath("$.orderItems[0].price", is(10)))
                .andExpect(jsonPath("$.orderItems[0].quantity", is(10)))
                .andExpect(jsonPath("$.orderItems[0].orderItemId", notNullValue()));
    }
}
