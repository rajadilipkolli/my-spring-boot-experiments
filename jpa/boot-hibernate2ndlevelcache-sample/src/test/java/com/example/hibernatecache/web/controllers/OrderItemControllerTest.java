package com.example.hibernatecache.web.controllers;

import static com.example.hibernatecache.utils.AppConstants.PROFILE_TEST;
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

import com.example.hibernatecache.entities.Customer;
import com.example.hibernatecache.entities.Order;
import com.example.hibernatecache.entities.OrderItem;
import com.example.hibernatecache.model.response.OrderItemResponse;
import com.example.hibernatecache.model.response.PagedResult;
import com.example.hibernatecache.services.OrderItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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

@WebMvcTest(controllers = OrderItemController.class)
@ActiveProfiles(PROFILE_TEST)
class OrderItemControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private OrderItemService orderItemService;

    @Autowired private ObjectMapper objectMapper;

    private List<OrderItem> orderItemList;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        Customer savedCustomer =
                new Customer(
                        null, "firstName 1", "lastName 1", "email1@junit.com", "9876543211", null);
        savedOrder = new Order(null, "First Order", BigDecimal.TEN, savedCustomer, null);
        this.orderItemList = new ArrayList<>();
        this.orderItemList.add(new OrderItem(1L, "text 1", savedOrder));
        this.orderItemList.add(new OrderItem(2L, "text 2", savedOrder));
        this.orderItemList.add(new OrderItem(3L, "text 3", savedOrder));
    }

    @Test
    void shouldFetchAllOrderItems() throws Exception {
        List<OrderItemResponse> orderItemResponseList =
                List.of(
                        new OrderItemResponse(1L, "text 1"),
                        new OrderItemResponse(2L, "text 2"),
                        new OrderItemResponse(3L, "text 3"));
        Page<OrderItemResponse> page = new PageImpl<>(orderItemResponseList);
        PagedResult<OrderItemResponse> orderItemPagedResult = new PagedResult<>(page);
        given(orderItemService.findAllOrderItems(0, 10, "id", "asc"))
                .willReturn(orderItemPagedResult);

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
        Long orderItemId = 1L;
        OrderItemResponse orderItem = new OrderItemResponse(orderItemId, "text 1");
        given(orderItemService.findOrderItemById(orderItemId)).willReturn(Optional.of(orderItem));

        this.mockMvc
                .perform(get("/api/order/items/{id}", orderItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(orderItem.text())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingOrderItem() throws Exception {
        Long orderItemId = 1L;
        given(orderItemService.findOrderItemById(orderItemId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/order/items/{id}", orderItemId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewOrderItem() throws Exception {
        given(orderItemService.saveOrderItem(any(OrderItem.class)))
                .willReturn(new OrderItemResponse(1L, "some text"));

        OrderItem orderItem = new OrderItem(1L, "some text", savedOrder);
        this.mockMvc
                .perform(
                        post("/api/order/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderItemId", notNullValue()))
                .andExpect(jsonPath("$.text", is(orderItem.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewOrderItemWithoutText() throws Exception {
        OrderItem orderItem = new OrderItem(null, null, savedOrder);

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
        Long orderItemId = 1L;
        OrderItem orderItem = new OrderItem(orderItemId, "Updated text", savedOrder);
        given(orderItemService.findById(orderItemId)).willReturn(Optional.of(orderItem));
        given(orderItemService.updateOrder(any(OrderItem.class)))
                .willReturn(new OrderItemResponse(orderItemId, "Updated text"));

        this.mockMvc
                .perform(
                        put("/api/order/items/{id}", orderItem.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(orderItem.getText())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingOrderItem() throws Exception {
        Long orderItemId = 1L;
        given(orderItemService.findOrderItemById(orderItemId)).willReturn(Optional.empty());
        OrderItem orderItem = new OrderItem(orderItemId, "Updated text", savedOrder);

        this.mockMvc
                .perform(
                        put("/api/order/items/{id}", orderItemId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderItem)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteOrderItem() throws Exception {
        Long orderItemId = 1L;
        OrderItemResponse orderItem = new OrderItemResponse(orderItemId, "Some text");
        given(orderItemService.findOrderItemById(orderItemId)).willReturn(Optional.of(orderItem));
        doNothing().when(orderItemService).deleteOrderItemById(orderItemId);

        this.mockMvc
                .perform(delete("/api/order/items/{id}", orderItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(orderItem.text())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingOrderItem() throws Exception {
        Long orderItemId = 1L;
        given(orderItemService.findOrderItemById(orderItemId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/order/items/{id}", orderItemId))
                .andExpect(status().isNotFound());
    }
}
