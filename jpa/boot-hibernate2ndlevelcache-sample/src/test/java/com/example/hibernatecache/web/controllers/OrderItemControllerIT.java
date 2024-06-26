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
import com.example.hibernatecache.repositories.CustomerRepository;
import com.example.hibernatecache.repositories.OrderItemRepository;
import com.example.hibernatecache.repositories.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class OrderItemControllerIT extends AbstractIntegrationTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private List<OrderItem> orderItemList = null;
    Order savedOrder;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        customerRepository.deleteAll();

        Customer savedCustomer = customerRepository.persist(new Customer()
                .setFirstName("firstName 1")
                .setLastName("lastName 1")
                .setEmail("email1@junit.com")
                .setPhone("9876543211")
                .addOrder(new Order()
                        .setName("First Order")
                        .setPrice(BigDecimal.TEN)
                        .addOrderItem(new OrderItem().setText("First OrderItem"))
                        .addOrderItem(new OrderItem().setText("Second OrderItem"))
                        .addOrderItem(new OrderItem().setText("Third OrderItem"))));
        savedOrder = savedCustomer.getOrders().getFirst();
        orderItemList = savedOrder.getOrderItems();
    }

    @Test
    void shouldFetchAllOrderItems() throws Exception {
        this.mockMvc
                .perform(get("/api/order/items"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.data.size()", is(orderItemList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindOrderItemById() throws Exception {
        OrderItem orderItem = orderItemList.getFirst();
        Long orderItemId = orderItem.getId();

        this.mockMvc
                .perform(get("/api/order/items/{id}", orderItemId))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.orderItemId", is(orderItem.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(orderItem.getText())));
    }

    @Test
    void shouldCreateNewOrderItem() throws Exception {
        OrderItemRequest orderItemRequest = new OrderItemRequest("New OrderItem", savedOrder.getId());
        this.mockMvc
                .perform(post("/api/order/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderItemRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.orderItemId", notNullValue()))
                .andExpect(jsonPath("$.text", is(orderItemRequest.text())));
    }

    @Test
    void shouldReturn400WhenCreateNewOrderItemWithoutText() throws Exception {
        OrderItemRequest orderItemRequest = new OrderItemRequest(null, null);

        this.mockMvc
                .perform(post("/api/order/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderItemRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/order/items")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateOrderItem() throws Exception {
        Long orderItemId = orderItemList.getFirst().getId();
        OrderItemRequest orderItemRequest = new OrderItemRequest("Updated OrderItem", savedOrder.getId());

        this.mockMvc
                .perform(put("/api/order/items/{id}", orderItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderItemRequest)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.orderItemId", is(orderItemId), Long.class))
                .andExpect(jsonPath("$.text", is(orderItemRequest.text())));
    }

    @Test
    void shouldDeleteOrderItem() throws Exception {
        OrderItem orderItem = orderItemList.getFirst();

        this.mockMvc
                .perform(delete("/api/order/items/{id}", orderItem.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.orderItemId", is(orderItem.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(orderItem.getText())));
    }
}
