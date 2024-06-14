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

import com.example.custom.sequence.entities.Customer;
import com.example.custom.sequence.entities.Order;
import com.example.custom.sequence.model.response.CustomerResponse;
import com.example.custom.sequence.model.response.OrderResponse;
import com.example.custom.sequence.model.response.PagedResult;
import com.example.custom.sequence.services.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
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
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer(null, "customer1", new ArrayList<>());
        this.orderList = new ArrayList<>();
        this.orderList.add(new Order("1", "text 1", customer));
        this.orderList.add(new Order("2", "text 2", customer));
        this.orderList.add(new Order("3", "text 3", customer));
    }

    @Test
    void shouldFetchAllOrders() throws Exception {
        PagedResult<OrderResponse> orderPagedResult = getOrderResponsePagedResult();
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

    private static @NotNull PagedResult<OrderResponse> getOrderResponsePagedResult() {
        List<OrderResponse> orderResponseList = new ArrayList<>();
        orderResponseList.add(
                new OrderResponse("1", "text 1", new CustomerResponse("1", "customer1")));
        orderResponseList.add(
                new OrderResponse("2", "text 2", new CustomerResponse("1", "customer1")));
        orderResponseList.add(
                new OrderResponse("3", "text 3", new CustomerResponse("1", "customer1")));
        Page<OrderResponse> page = new PageImpl<>(orderResponseList);
        return new PagedResult<>(page);
    }

    @Test
    void shouldFindOrderById() throws Exception {
        String orderId = "1";
        OrderResponse order =
                new OrderResponse(
                        orderId,
                        "text 1",
                        new CustomerResponse(customer.getId(), customer.getText()));
        given(orderService.findOrderById(orderId)).willReturn(Optional.of(order));

        this.mockMvc
                .perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(order.text())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingOrder() throws Exception {
        String orderId = "1";
        given(orderService.findOrderById(orderId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/api/orders/{id}", orderId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewOrder() throws Exception {
        given(orderService.saveOrder(any(Order.class)))
                .willReturn(new OrderResponse("1", "some text", null));

        Order order = new Order("1", "some text", customer);
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
        Order order = new Order(null, null, null);

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
        String orderId = "1";
        OrderResponse orderResponse =
                new OrderResponse(
                        orderId,
                        "Updated text",
                        new CustomerResponse(customer.getId(), customer.getText()));
        given(orderService.findOrderById(orderId)).willReturn(Optional.of(orderResponse));
        given(orderService.saveOrder(any(Order.class)))
                .willReturn(new OrderResponse("1", "Updated text", null));

        this.mockMvc
                .perform(
                        put("/api/orders/{id}", orderResponse.id())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderResponse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(orderResponse.text())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingOrder() throws Exception {
        String orderId = "1";
        given(orderService.findOrderById(orderId)).willReturn(Optional.empty());
        Order order = new Order(orderId, "Updated text", customer);

        this.mockMvc
                .perform(
                        put("/api/orders/{id}", orderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteOrder() throws Exception {
        String orderId = "1";
        OrderResponse order =
                new OrderResponse(
                        orderId,
                        "Some text",
                        new CustomerResponse(customer.getId(), customer.getText()));
        given(orderService.findOrderById(orderId)).willReturn(Optional.of(order));
        doNothing().when(orderService).deleteOrderById(order.id());

        this.mockMvc
                .perform(delete("/api/orders/{id}", order.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(order.text())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingOrder() throws Exception {
        String orderId = "1";
        given(orderService.findOrderById(orderId)).willReturn(Optional.empty());

        this.mockMvc.perform(delete("/api/orders/{id}", orderId)).andExpect(status().isNotFound());
    }
}
