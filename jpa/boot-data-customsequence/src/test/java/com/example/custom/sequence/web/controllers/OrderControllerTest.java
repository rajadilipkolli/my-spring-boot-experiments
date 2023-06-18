package com.example.custom.sequence.web.controllers;

import static com.example.custom.sequence.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.custom.sequence.entities.Order;
import com.example.custom.sequence.model.response.PagedResult;
import com.example.custom.sequence.services.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OrderController.class)
@ActiveProfiles(PROFILE_TEST)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private OrderService orderService;

    @Autowired private ObjectMapper objectMapper;

    private List<Order> orderList;

    @BeforeEach
    void setUp() {
        this.orderList = new ArrayList<>();
        this.orderList.add(new Order(1L, "text 1"));
        this.orderList.add(new Order(2L, "text 2"));
        this.orderList.add(new Order(3L, "text 3"));
    }

    @Test
    void shouldFetchAllOrders() throws Exception {
        Page<Order> page = new PageImpl<>(orderList);
        PagedResult<Order> orderPagedResult = new PagedResult<>(page);
        given(orderService.findAllOrders(0, 10, "id", "asc")).willReturn(orderPagedResult);

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
        Long orderId = 1L;
        Order order = new Order(orderId, "text 1");
        given(orderService.findOrderById(orderId)).willReturn(Optional.of(order));

        this.mockMvc
                .perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(order.getText())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingOrder() throws Exception {
        Long orderId = 1L;
        given(orderService.findOrderById(orderId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/api/orders/{id}", orderId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewOrder() throws Exception {
        given(orderService.saveOrder(any(Order.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        Order order = new Order(1L, "some text");
        this.mockMvc
                .perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(order.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewOrderWithoutText() throws Exception {
        Order order = new Order(null, null);

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
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateOrder() throws Exception {
        Long orderId = 1L;
        Order order = new Order(orderId, "Updated text");
        given(orderService.findOrderById(orderId)).willReturn(Optional.of(order));
        given(orderService.saveOrder(any(Order.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/api/orders/{id}", order.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(order.getText())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingOrder() throws Exception {
        Long orderId = 1L;
        given(orderService.findOrderById(orderId)).willReturn(Optional.empty());
        Order order = new Order(orderId, "Updated text");

        this.mockMvc
                .perform(
                        put("/api/orders/{id}", orderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteOrder() throws Exception {
        Long orderId = 1L;
        Order order = new Order(orderId, "Some text");
        given(orderService.findOrderById(orderId)).willReturn(Optional.of(order));
        doNothing().when(orderService).deleteOrderById(order.getId());

        this.mockMvc
                .perform(delete("/api/orders/{id}", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(order.getText())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingOrder() throws Exception {
        Long orderId = 1L;
        given(orderService.findOrderById(orderId)).willReturn(Optional.empty());

        this.mockMvc.perform(delete("/api/orders/{id}", orderId)).andExpect(status().isNotFound());
    }
}
