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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;

class OrderControllerIT extends AbstractIntegrationTest {

    @Autowired private OrderRepository orderRepository;
    @Autowired private TenantIdentifierResolver tenantIdentifierResolver;
@Autowired private JdbcTemplate jdbcTemplate;

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
@Test
    void shouldVerifyPhysicalPartitioning() {
        // Ensure correct tenant is set
        tenantIdentifierResolver.setCurrentTenant("dbsystc");
        
        // 1. Verify that the partitioned tables exist in the database
        List

<String> partitionTables = jdbcTemplate.queryForList(
                "SELECT c.relname AS child_table " +
                "FROM pg_inherits i " +
                "JOIN pg_class p ON p.oid = i.inhparent " +
                "JOIN pg_class c ON c.oid = i.inhrelid " +
                "JOIN pg_namespace n ON n.oid = c.relnamespace " +
                "WHERE p.relname = 'orders' " +
                "ORDER BY c.relname",
                String.class);
        
        // Ensure that partition tables exist (e.g., orders_2022, orders_2023, orders_2024, orders_2025)
        assertThat(partitionTables).isNotEmpty();
        assertThat(partitionTables).contains("orders_2022", "orders_2023", "orders_2024", "orders_2025");
        
        // 2. Verify that orders are stored in the correct partitions based on order dates
        Order order2025 = orderList.stream()
                .filter(o -> o.getOrderDate().getYear() == 2025)
                .findFirst()
                .orElseThrow();
        
        // Confirm the order exists within the 2025 partition
        Integer countIn2025 = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders_2025 WHERE id = ?",
                Integer.class,
                order2025.getId());
        assertThat(countIn2025).isEqualTo(1);
        
        // Confirm the order does not exist in the 2024 partition
        Integer countIn2024 = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders_2024 WHERE id = ?",
                Integer.class,
                order2025.getId());
        assertThat(countIn2024).isZero();
        
        // 3. Verify the overall order count within each partition matches the test data
        verifyOrdersInPartition(2022, 1); // Expected 1 order in 2022
        verifyOrdersInPartition(2023, 1); // Expected 1 order in 2023
        verifyOrdersInPartition(2024, 1); // Expected 1 order in 2024
        verifyOrdersInPartition(2025, 3); // Expected 3 orders in 2025
    }
    
    /**
     * Helper method to verify that the correct number of orders exist in a specific year's partition.
     *
     * @param year The year corresponding to the partition.
     * @param expectedCount The expected number of orders in that partition.
     */
    private void verifyOrdersInPartition(int year, int expectedCount) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders_" + year,
                Integer.class);
        assertThat(count).isEqualTo(expectedCount);
    }

}
