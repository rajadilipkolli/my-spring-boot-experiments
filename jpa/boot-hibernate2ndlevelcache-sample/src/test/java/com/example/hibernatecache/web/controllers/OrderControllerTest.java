package com.example.hibernatecache.web.controllers;

import static com.example.hibernatecache.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.hibernatecache.entities.Customer;
import com.example.hibernatecache.entities.Order;
import com.example.hibernatecache.entities.OrderItem;
import com.example.hibernatecache.exception.OrderNotFoundException;
import com.example.hibernatecache.model.query.FindOrdersQuery;
import com.example.hibernatecache.model.request.OrderItemRequest;
import com.example.hibernatecache.model.request.OrderRequest;
import com.example.hibernatecache.model.response.OrderItemResponse;
import com.example.hibernatecache.model.response.OrderResponse;
import com.example.hibernatecache.model.response.PagedResult;
import com.example.hibernatecache.services.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

    final Customer customer = new Customer()
            .setId(1L)
            .setFirstName("firstName 1")
            .setLastName("lastName 1")
            .setEmail("email1@junit.com")
            .setPhone("9876543211");

    @BeforeEach
    void setUp() {
        this.orderList = new ArrayList<>();
        this.orderList.add(new Order()
                .setId(1L)
                .setName("text 1")
                .setPrice(BigDecimal.TEN)
                .setCustomer(customer)
                .setOrderItems(List.of(
                        new OrderItem().setId(1L).setPrice(BigDecimal.ONE).setQuantity(10))));
        this.orderList.add(new Order()
                .setId(2L)
                .setName("text 2")
                .setPrice(BigDecimal.TEN)
                .setCustomer(customer)
                .setOrderItems(List.of(
                        new OrderItem().setId(2L).setPrice(BigDecimal.TWO).setQuantity(5))));
        this.orderList.add(new Order()
                .setId(3L)
                .setName("text 3")
                .setPrice(BigDecimal.TEN)
                .setCustomer(customer)
                .setOrderItems(List.of(
                        new OrderItem().setId(3L).setPrice(BigDecimal.TEN).setQuantity(2))));
    }

    @Test
    void shouldFetchAllOrders() throws Exception {

        Page<Order> page = new PageImpl<>(orderList);
        PagedResult<OrderResponse> orderPagedResult = new PagedResult<>(page, getOrderResponseList());
        FindOrdersQuery findOrdersQuery = new FindOrdersQuery(0, 10, "id", "asc");
        given(orderService.findAllOrders(findOrdersQuery)).willReturn(orderPagedResult);

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
        OrderResponse order = new OrderResponse(1L, orderId, "text 1", BigDecimal.TEN, new ArrayList<>());
        given(orderService.findOrderById(orderId)).willReturn(Optional.of(order));

        this.mockMvc
                .perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(order.name())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingOrder() throws Exception {
        Long orderId = 1L;
        given(orderService.findOrderById(orderId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-hibernate2ndlevelcache-sample.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Order with Id '%d' not found".formatted(orderId)));
    }

    @Test
    void shouldCreateNewOrder() throws Exception {

        OrderRequest orderRequest =
                new OrderRequest(1L, "some text", List.of(new OrderItemRequest(BigDecimal.TEN, 10, "ORD1")));
        OrderResponse orderResponse = new OrderResponse(1L, 1L, "some text", BigDecimal.TEN, new ArrayList<>());
        given(orderService.saveOrder(any(OrderRequest.class))).willReturn(orderResponse);
        this.mockMvc
                .perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.orderId", notNullValue()))
                .andExpect(jsonPath("$.name", is(orderRequest.name())));
    }

    @Test
    void shouldReturn400WhenCreateNewOrderWithoutName() throws Exception {
        OrderRequest orderRequest = new OrderRequest(null, null, List.of());

        this.mockMvc
                .perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
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
                .andExpect(jsonPath("$.violations[1].field", is("orderItems")))
                .andExpect(jsonPath("$.violations[1].message", is("OrderItems are required")))
                .andReturn();
    }

    @Test
    void shouldUpdateOrder() throws Exception {
        Long orderId = 1L;
        OrderRequest orderRequest = new OrderRequest(
                customer.getId(), "Updated text", List.of(new OrderItemRequest(BigDecimal.TEN, 10, "ORD1")));
        OrderResponse orderResponse =
                new OrderResponse(customer.getId(), orderId, "New text", BigDecimal.TEN, new ArrayList<>());
        given(orderService.findOrderById(orderId)).willReturn(Optional.of(orderResponse));
        given(orderService.updateOrder(eq(orderId), any(OrderRequest.class)))
                .willReturn(new OrderResponse(
                        customer.getId(), orderId, "Updated text", BigDecimal.TEN, new ArrayList<>()));

        this.mockMvc
                .perform(put("/api/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(orderId), Long.class))
                .andExpect(jsonPath("$.name", is(orderRequest.name())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingOrder() throws Exception {
        Long orderId = 1L;
        OrderRequest orderRequest =
                new OrderRequest(orderId, "Updated text", List.of(new OrderItemRequest(BigDecimal.TEN, 10, "ORD1")));
        given(orderService.updateOrder(eq(orderId), any(OrderRequest.class)))
                .willThrow(new OrderNotFoundException(orderId));

        this.mockMvc
                .perform(put("/api/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-hibernate2ndlevelcache-sample.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Order with Id '%d' not found".formatted(orderId)));
    }

    @Test
    void shouldDeleteOrder() throws Exception {
        Long orderId = 1L;
        OrderResponse order =
                new OrderResponse(customer.getId(), orderId, "Some text", BigDecimal.TEN, new ArrayList<>());
        given(orderService.findOrderById(orderId)).willReturn(Optional.of(order));
        doNothing().when(orderService).deleteOrderById(orderId);

        this.mockMvc
                .perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(order.name())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingOrder() throws Exception {
        Long orderId = 1L;
        given(orderService.findOrderById(orderId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/orders/{id}", orderId))
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-hibernate2ndlevelcache-sample.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Order with Id '%d' not found".formatted(orderId)));
    }

    List<OrderResponse> getOrderResponseList() {
        return orderList.stream()
                .map(order -> new OrderResponse(
                        order.getCustomer().getId(),
                        order.getId(),
                        order.getName(),
                        order.getPrice(),
                        getOrderItemResponse(order.getOrderItems())))
                .toList();
    }

    private List<OrderItemResponse> getOrderItemResponse(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(orderItem -> new OrderItemResponse(
                        orderItem.getId(), orderItem.getItemCode(), orderItem.getPrice(), orderItem.getQuantity()))
                .toList();
    }
}
