package com.example.custom.sequence.web.controllers;

import static com.example.custom.sequence.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.custom.sequence.entities.Customer;
import com.example.custom.sequence.entities.Order;
import com.example.custom.sequence.model.request.OrderRequest;
import com.example.custom.sequence.model.response.CustomerResponseWithOutOrder;
import com.example.custom.sequence.model.response.OrderResponse;
import com.example.custom.sequence.model.response.PagedResult;
import com.example.custom.sequence.services.OrderService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = OrderController.class)
@ActiveProfiles(PROFILE_TEST)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<Order> orderList;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer().setId("CUST_01").setText("customer1");
        this.orderList = new ArrayList<>();
        this.orderList.add(new Order().setId("1").setText("text 1").setCustomer(customer));
        this.orderList.add(new Order().setId("2").setText("text 2").setCustomer(customer));
        this.orderList.add(new Order().setId("3").setText("text 3").setCustomer(customer));
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

    private static @NonNull PagedResult<OrderResponse> getOrderResponsePagedResult() {
        List<OrderResponse> orderResponseList = new ArrayList<>();
        orderResponseList.add(new OrderResponse("1", "text 1", new CustomerResponseWithOutOrder("1", "customer1")));
        orderResponseList.add(new OrderResponse("2", "text 2", new CustomerResponseWithOutOrder("1", "customer1")));
        orderResponseList.add(new OrderResponse("3", "text 3", new CustomerResponseWithOutOrder("1", "customer1")));
        Page<OrderResponse> page = new PageImpl<>(orderResponseList);
        return new PagedResult<>(page);
    }

    @Test
    void shouldFindOrderById() throws Exception {
        String orderId = "1";
        OrderResponse order = new OrderResponse(
                orderId, "text 1", new CustomerResponseWithOutOrder(customer.getId(), customer.getText()));
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

        OrderRequest orderRequest = new OrderRequest("some text", customer.getId());
        given(orderService.saveOrder(orderRequest)).willReturn(Optional.of(new OrderResponse("1", "some text", null)));
        this.mockMvc
                .perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(orderRequest.text())));
    }

    @Test
    void shouldReturn400WhenCreateNewOrderWithoutText() throws Exception {
        OrderRequest order = new OrderRequest(null, null);

        this.mockMvc
                .perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("https://custom-sequence.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/orders")))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("customerId")))
                .andExpect(jsonPath("$.violations[0].message", is("CustomerId cannot be blank")))
                .andExpect(jsonPath("$.violations[1].field", is("text")))
                .andExpect(jsonPath("$.violations[1].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateOrder() throws Exception {
        String orderId = "1";
        OrderResponse orderResponse = new OrderResponse(
                orderId, "Updated text", new CustomerResponseWithOutOrder(customer.getId(), customer.getText()));
        OrderRequest orderRequest = new OrderRequest("Updated text", customer.getId());
        given(orderService.updateOrderById(orderId, orderRequest)).willReturn(Optional.of(orderResponse));

        this.mockMvc
                .perform(put("/api/orders/{id}", orderResponse.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(orderResponse.text())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingOrder() throws Exception {
        String orderId = "1";
        OrderRequest orderRequest = new OrderRequest("Updated text", customer.getId());
        given(orderService.updateOrderById(orderId, orderRequest)).willReturn(Optional.empty());

        this.mockMvc
                .perform(put("/api/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).updateOrderById(orderId, orderRequest);
    }

    @Test
    void shouldDeleteOrder() throws Exception {
        String orderId = "1";
        OrderResponse order = new OrderResponse(
                orderId, "Some text", new CustomerResponseWithOutOrder(customer.getId(), customer.getText()));
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
