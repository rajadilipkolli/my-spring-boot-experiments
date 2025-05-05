package com.example.hibernatecache.common;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.hibernatecache.model.request.CustomerRequest;
import com.example.hibernatecache.model.request.OrderItemRequest;
import com.example.hibernatecache.model.request.OrderRequest;
import io.hypersistence.utils.jdbc.validator.SQLStatementCountValidator;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/**
 * Integration tests focused on testing Hibernate 2nd level cache with entity relationships.
 * This class specifically tests entity-level caching and collection relationships
 */
class EntityCacheIT extends AbstractIntegrationTest {

    private Long testCustomerId;
    private Long testOrderId;

    @BeforeEach
    void setUp() throws Exception {
        // Clean database between tests - first find existing entities
        List<Long> orderItems = orderItemRepository.findAllOrderItemIds();
        List<Long> orders = orderRepository.findAllOrderIds();
        List<Long> customers = customerRepository.findAllCustomerIds();

        // Delete in proper order to avoid constraint violations
        if (!orderItems.isEmpty()) {
            orderItems.forEach(itemId -> orderItemRepository.deleteById(itemId));
        }
        if (!orders.isEmpty()) {
            orders.forEach(orderId -> orderRepository.deleteById(orderId));
        }
        if (!customers.isEmpty()) {
            customers.forEach(customerId -> customerRepository.deleteById(customerId));
        }

        // Create test data
        CustomerRequest customerRequest = new CustomerRequest("CacheTest", "LastName", "cache@test.com", "1234567890");

        // Create a test customer with an order
        String response = this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        testCustomerId = objectMapper.readTree(response).path("customerId").asLong();

        // Add an order with multiple items
        OrderRequest orderRequest = new OrderRequest(
                testCustomerId,
                "Test Order",
                List.of(
                        new OrderItemRequest(BigDecimal.valueOf(10.99), 2, "ITEM-1"),
                        new OrderItemRequest(BigDecimal.valueOf(5.99), 1, "ITEM-2")));

        response = this.mockMvc
                .perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        testOrderId = objectMapper.readTree(response).path("orderId").asLong();

        // Reset SQL counter after setup
        SQLStatementCountValidator.reset();
    }

    @Test
    void shouldCacheEntityAndRelationship() throws Exception {
        // First request - should hit DB
        this.mockMvc
                .perform(get("/api/customers/{id}", testCustomerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("CacheTest")));

        // Should have DB queries for customer, 1 - order, 1 - order items
        SQLStatementCountValidator.assertSelectCount(2);
        SQLStatementCountValidator.assertTotalCount(2);
        SQLStatementCountValidator.reset();

        // Second request - should use cache
        this.mockMvc
                .perform(get("/api/customers/{id}", testCustomerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("CacheTest")));

        // Should have no DB queries for customer (served from cache)
        SQLStatementCountValidator.assertSelectCount(0);
        SQLStatementCountValidator.assertTotalCount(0);
        SQLStatementCountValidator.reset();

        // Now fetch the order with its relationship
        this.mockMvc
                .perform(get("/api/orders/{id}", testOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Order")));

        // Should have DB queries for order and items
        SQLStatementCountValidator.assertSelectCount(1);
        SQLStatementCountValidator.assertTotalCount(1);
        SQLStatementCountValidator.reset();

        // Second request for order - should use cache
        this.mockMvc
                .perform(get("/api/orders/{id}", testOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Order")));

        // Should have no DB queries (served from cache)
        SQLStatementCountValidator.assertSelectCount(0);
        SQLStatementCountValidator.assertTotalCount(0);
    }

    @Test
    void shouldCacheCollectionRelationship() throws Exception {
        // First get order items for an order
        this.mockMvc
                .perform(get("/api/orders/{id}/items", testOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)));

        // Should have DB query for collection
        SQLStatementCountValidator.assertSelectCount(1);
        SQLStatementCountValidator.assertTotalCount(1);
        SQLStatementCountValidator.reset();

        // Second request - should use cache
        this.mockMvc
                .perform(get("/api/orders/{id}/items", testOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)));

        // Should have no DB queries (served from cache)
        SQLStatementCountValidator.assertSelectCount(0);
        SQLStatementCountValidator.assertTotalCount(0);
    }

    @Test
    void shouldEvictCollectionCacheOnItemUpdate() throws Exception {
        // First get all order items - will be cached
        String response = this.mockMvc
                .perform(get("/api/orders/{id}/items", testOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Get first order item ID
        Long orderItemId =
                objectMapper.readTree(response).get(0).path("orderItemId").asLong();

        SQLStatementCountValidator.reset();

        // Second get from cache - no DB hit
        this.mockMvc
                .perform(get("/api/orders/{id}/items", testOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)));

        SQLStatementCountValidator.assertSelectCount(0);
        SQLStatementCountValidator.assertTotalCount(0);

        // Update order item
        OrderItemRequest updatedRequest = new OrderItemRequest(BigDecimal.valueOf(15.99), 3, "UPDATED-ITEM");

        this.mockMvc
                .perform(put("/api/order/items/{id}", orderItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCode", is("UPDATED-ITEM")));

        SQLStatementCountValidator.reset();

        // Get order items again - should hit DB because collection cache was invalidated
        this.mockMvc
                .perform(get("/api/orders/{id}/items", testOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)));

        // Should have DB queries because cache was invalidated
        SQLStatementCountValidator.assertSelectCount(1);
        SQLStatementCountValidator.assertTotalCount(1);
    }

    @Test
    void shouldEvictCacheOnEntityDelete() throws Exception {
        // Create additional order to be deleted
        OrderRequest orderRequest = new OrderRequest(
                testCustomerId,
                "Order To Delete",
                List.of(new OrderItemRequest(BigDecimal.valueOf(9.99), 1, "DELETE-ITEM")));

        String response = this.mockMvc
                .perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long orderToDeleteId = objectMapper.readTree(response).path("orderId").asLong();

        // -------------------- STEP 1: Verify there are 2 orders initially --------------------
        // Reset the SQL counter before our test
        SQLStatementCountValidator.reset();

        this.mockMvc
                .perform(get("/api/customers/{id}/orders", testCustomerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)));

        // -------------------- STEP 2: Delete an order --------------------
        // Delete the order we just created
        this.mockMvc.perform(delete("/api/orders/{id}", orderToDeleteId)).andExpect(status().isAccepted());

        // Reset SQL counter after deletion
        SQLStatementCountValidator.reset();

        // -------------------- STEP 3: Verify orders list is updated and cache is invalidated --------------------
        // Now get orders again - should now have only 1 order left
        this.mockMvc
                .perform(get("/api/customers/{id}/orders", testCustomerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)));

        // The key test is that the database is hit because the cache was invalidated, 1- customer exists, 1-order, 1-
        // customer
        SQLStatementCountValidator.assertSelectCount(3);
    }
}
