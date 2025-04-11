package com.example.multitenancy.partition.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.multitenancy.partition.common.AbstractIntegrationTest;
import com.example.multitenancy.partition.config.tenant.TenantIdentifierResolver;
import com.example.multitenancy.partition.entities.Order;
import com.example.multitenancy.partition.repositories.OrderRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class OrderControllerIT extends AbstractIntegrationTest {

    @Autowired private OrderRepository orderRepository;
    @Autowired private TenantIdentifierResolver tenantIdentifierResolver;

    private List<Order> orderList;

    @BeforeEach
    void setUp() {
        tenantIdentifierResolver.setCurrentTenant("dbsystc");
        orderRepository.deleteAll();

        orderList = new ArrayList<>();
        orderList.add(new Order(null, 100.0, LocalDate.of(2025, 1, 1)));
        orderList.add(new Order(null, 200.0, LocalDate.of(2025, 6, 1)));
        orderList.add(new Order(null, 300.0, LocalDate.of(2025, 12, 31)));
        orderList.add(new Order(null, 150.0, LocalDate.of(2023, 1, 1)));
        orderList.add(new Order(null, 250.0, LocalDate.of(2024, 6, 1)));
        orderList.add(new Order(null, 350.0, LocalDate.of(2022, 12, 31)));
        orderList = orderRepository.saveAll(orderList);
    }

    @Test
    void shouldFetchAllOrders() throws Exception {
        this.mockMvc
                .perform(get("/api/orders").param("tenant", "dbsystc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(orderList.size())));
    }

    @Test
    void shouldFindOrderById() throws Exception {
        Order order = orderList.getFirst();
        Long orderId = order.getId();

        this.mockMvc
                .perform(get("/api/orders/{id}", orderId).param("tenant", "dbsystc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(order.getAmount())));
    }

    @Test
    void shouldCreateNewOrder() throws Exception {
        Order order = new Order(null, 400.0, LocalDate.of(2025, 7, 1));
        long count = this.orderRepository.count();
        this.mockMvc
                .perform(
                        post("/api/orders")
                                .param("tenant", "dbsystc")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.amount", is(order.getAmount())));
        assertThat(this.orderRepository.count()).isEqualTo(count + 1);
    }

    @Test
    void shouldUpdateOrder() throws Exception {
        Order order = orderList.getFirst();
        order.setAmount(500.0);

        this.mockMvc
                .perform(
                        put("/api/orders/{id}", order.getId())
                                .param("tenant", "dbsystc")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(order.getAmount())));
    }

    @Test
    void shouldDeleteOrder() throws Exception {
        Order order = orderList.getFirst();
        long count = this.orderRepository.count();
        this.mockMvc
                .perform(delete("/api/orders/{id}", order.getId()).param("tenant", "dbsystc"))
                .andExpect(status().isOk());
        assertThat(this.orderRepository.count()).isEqualTo(count - 1);
    }

    @Test
    void shouldVerifyPartitionByRange() {
        // Query orders within the 2025 partition range
        List<Order> ordersIn2025 =
                orderRepository.findAll().stream()
                        .filter(
                                order ->
                                        order.getOrderDate().isAfter(LocalDate.of(2024, 12, 31))
                                                && order.getOrderDate()
                                                        .isBefore(LocalDate.of(2026, 1, 1)))
                        .toList();

        // Fixing the assertion to match the expected size of orders in 2025
        assertThat(ordersIn2025).hasSize(3);
        assertThat(ordersIn2025)
                .allMatch(
                        order ->
                                order.getOrderDate().isAfter(LocalDate.of(2024, 12, 31))
                                        && order.getOrderDate().isBefore(LocalDate.of(2026, 1, 1)));
    }
}
