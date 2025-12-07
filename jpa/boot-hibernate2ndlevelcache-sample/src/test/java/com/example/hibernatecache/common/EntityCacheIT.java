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
        orderItemRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        customerRepository.deleteAllInBatch();

        // Create test data
        CustomerRequest customerRequest = new CustomerRequest("CacheTest", "LastName", "cache@test.com", "1234567890");

        // Create a test customer with an order
        String response = this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        testCustomerId = jsonMapper.readTree(response).path("customerId").asLong();

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
                        .content(jsonMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        testOrderId = jsonMapper.readTree(response).path("orderId").asLong();

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
                jsonMapper.readTree(response).get(0).path("orderItemId").asLong();

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
                        .content(jsonMapper.writeValueAsString(updatedRequest)))
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
                        .content(jsonMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long orderToDeleteId = jsonMapper.readTree(response).path("orderId").asLong();

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

    @Test
    void shouldEvictCacheOnOrderItemDelete() throws Exception {
        // Get order items to cache them first
        String response = this.mockMvc
                .perform(get("/api/orders/{id}/items", testOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Get first order item ID
        Long orderItemId =
                jsonMapper.readTree(response).get(0).path("orderItemId").asLong();

        // Ensure items are in cache by making a second request - this should hit cache
        SQLStatementCountValidator.reset();

        this.mockMvc
                .perform(get("/api/orders/{id}/items", testOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)));

        // Verify no SQL queries were executed (served from cache)
        SQLStatementCountValidator.assertSelectCount(0);
        SQLStatementCountValidator.assertTotalCount(0);

        // -------------------- STEP 1: Delete an order item --------------------
        this.mockMvc.perform(delete("/api/order/items/{id}", orderItemId)).andExpect(status().isOk());

        // Reset SQL counter after deletion
        SQLStatementCountValidator.assertDeleteCount(1);
        SQLStatementCountValidator.reset();

        // -------------------- STEP 2: Verify order items list is updated and cache is invalidated --------------------
        // Now get order items again - should now have only 1 item left
        this.mockMvc
                .perform(get("/api/orders/{id}/items", testOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)));

        // The cache should be invalidated, causing a database query
        SQLStatementCountValidator.assertSelectCount(1);
        SQLStatementCountValidator.assertTotalCount(1);

        // -------------------- STEP 3: Verify parent order cache is also invalidated --------------------
        SQLStatementCountValidator.reset();

        // Get the order again - should also hit database since its items collection was modified
        this.mockMvc
                .perform(get("/api/orders/{id}", testOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderItems.size()", is(1)));

        // Order cache should be invalidated due to child collection change
        SQLStatementCountValidator.assertSelectCount(1);
        SQLStatementCountValidator.assertTotalCount(1);
    }
}
