package com.example.hibernatecache.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.hibernatecache.common.AbstractIntegrationTest;
import com.example.hibernatecache.entities.Customer;
import com.example.hibernatecache.entities.Order;
import com.example.hibernatecache.entities.OrderItem;
import com.example.hibernatecache.model.request.OrderItemRequest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class OrderItemControllerIT extends AbstractIntegrationTest {

    private List<OrderItem> orderItemList = null;
    Order savedOrder;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAllInBatch(); // Delete parent first to cascade
        orderRepository.deleteAllInBatch();
        orderItemRepository.deleteAllInBatch();

        Customer savedCustomer = customerRepository.persist(createTestCustomer());
        savedOrder = savedCustomer.getOrders().getFirst();
        orderItemList = savedOrder.getOrderItems();
    }

    private Customer createTestCustomer() {
        return new Customer()
                .setFirstName("firstName 1")
                .setLastName("lastName 1")
                .setEmail("email1@junit.com")
                .setPhone("9876543211")
                .addOrder(createTestOrder());
    }

    private Order createTestOrder() {
        return new Order()
                .setName("First Order")
                .setPrice(BigDecimal.valueOf(111))
                .addOrderItem(createOrderItem(BigDecimal.TEN, 10, "ITM1"))
                .addOrderItem(createOrderItem(BigDecimal.TWO, 5, "ITM2"))
                .addOrderItem(createOrderItem(BigDecimal.ONE, 1, "ITM3"));
    }

    private OrderItem createOrderItem(BigDecimal price, int quantity, String itemCode) {
        return new OrderItem().setPrice(price).setQuantity(quantity).setItemCode(itemCode);
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
                .andExpect(jsonPath("$.price", is(orderItem.getPrice()), BigDecimal.class))
                .andExpect(jsonPath("$.quantity", is(orderItem.getQuantity())))
                .andExpect(jsonPath("$.itemCode", is(orderItem.getItemCode())));
    }

    @Test
    void shouldUpdateOrderItem() throws Exception {
        Long orderItemId = orderItemList.getFirst().getId();
        OrderItemRequest orderItemRequest = new OrderItemRequest(new BigDecimal("200.09"), 20, "ITM1");

        this.mockMvc
                .perform(put("/api/order/items/{id}", orderItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(orderItemRequest)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.orderItemId", is(orderItemId), Long.class))
                .andExpect(jsonPath("$.price", is(200.09)))
                .andExpect(jsonPath("$.quantity", is(orderItemRequest.quantity())))
                .andExpect(jsonPath("$.itemCode", is(orderItemRequest.itemCode())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingOrderItem() throws Exception {
        Long orderItemId =
                orderItemList.stream().mapToLong(OrderItem::getId).max().orElse(0L) + 1;
        OrderItemRequest orderItemRequest = new OrderItemRequest(BigDecimal.ONE, 10, "IMT3");

        this.mockMvc
                .perform(put("/api/order/items/{id}", orderItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(orderItemRequest)))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(
                        jsonPath("$.type", is("https://api.boot-hibernate2ndlevelcache-sample.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("OrderItem with Id '%d' not found".formatted(orderItemId)));
    }

    @Test
    void shouldDeleteOrderItem() throws Exception {
        OrderItem orderItem = orderItemList.getFirst();

        this.mockMvc
                .perform(delete("/api/order/items/{id}", orderItem.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.orderItemId", is(orderItem.getId()), Long.class))
                .andExpect(jsonPath("$.price", is(orderItem.getPrice()), BigDecimal.class))
                .andExpect(jsonPath("$.quantity", is(orderItem.getQuantity())))
                .andExpect(jsonPath("$.itemCode", is(orderItem.getItemCode())));
    }
}
