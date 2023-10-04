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
import com.example.hibernatecache.entities.OrderItem;
import com.example.hibernatecache.repositories.OrderItemRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class OrderItemControllerIT extends AbstractIntegrationTest {

    @Autowired private OrderItemRepository orderItemRepository;

    private List<OrderItem> orderItemList = null;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAllInBatch();

        orderItemList = new ArrayList<>();
        orderItemList.add(new OrderItem(null, "First OrderItem"));
        orderItemList.add(new OrderItem(null, "Second OrderItem"));
        orderItemList.add(new OrderItem(null, "Third OrderItem"));
        orderItemList = orderItemRepository.saveAll(orderItemList);
    }

    @Test
    void shouldFetchAllOrderItems() throws Exception {
        this.mockMvc
                .perform(get("/api/order/items"))
                .andExpect(status().isOk())
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
        OrderItem orderItem = orderItemList.get(0);
        Long orderItemId = orderItem.getId();

        this.mockMvc
                .perform(get("/api/order/items/{id}", orderItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderItem.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(orderItem.getText())));
    }

    @Test
    void shouldCreateNewOrderItem() throws Exception {
        OrderItem orderItem = new OrderItem(null, "New OrderItem");
        this.mockMvc
                .perform(
                        post("/api/order/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(orderItem.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewOrderItemWithoutText() throws Exception {
        OrderItem orderItem = new OrderItem(null, null);

        this.mockMvc
                .perform(
                        post("/api/order/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItem)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
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
        OrderItem orderItem = orderItemList.get(0);
        orderItem.setText("Updated OrderItem");

        this.mockMvc
                .perform(
                        put("/api/order/items/{id}", orderItem.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderItem.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(orderItem.getText())));
    }

    @Test
    void shouldDeleteOrderItem() throws Exception {
        OrderItem orderItem = orderItemList.get(0);

        this.mockMvc
                .perform(delete("/api/order/items/{id}", orderItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderItem.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(orderItem.getText())));
    }
}
