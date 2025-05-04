package com.example.hibernatecache;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.hibernatecache.common.AbstractIntegrationTest;
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
 * Integration tests specifically focused on testing Hibernate 2nd level cache behavior.
 */
class HibernateSecondLevelCacheIT extends AbstractIntegrationTest {

    @BeforeEach
    void setUp() {
        // Clean up existing data
        orderRepository.deleteAll();
        customerRepository.deleteAll();
        orderItemRepository.deleteAll();

        // Reset SQL counter
        SQLStatementCountValidator.reset();
    }

    @Test
    void shouldCacheCustomerEntity() throws Exception {
        // Create test customer
        CustomerRequest customerRequest =
                new CustomerRequest("CacheTest", "Customer", "cachetest@example.com", "1234567890");

        // First request - should hit the database
        SQLStatementCountValidator.reset();
        this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated());

        // Verify DB was hit for insert
        SQLStatementCountValidator.assertInsertCount(1);
        // For selecting next sequence value
        SQLStatementCountValidator.assertSelectCount(1);
        SQLStatementCountValidator.assertTotalCount(2);
        SQLStatementCountValidator.reset();

        // Multiple reads of the same customer by firstName should only hit DB once
        for (int i = 0; i < 5; i++) {
            this.mockMvc
                    .perform(get("/api/customers/search")
                            .param("firstName", "CacheTest")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName", is("CacheTest")));
        }

        // Verify only one select was executed despite 5 API calls
        SQLStatementCountValidator.assertSelectCount(1);
        SQLStatementCountValidator.assertTotalCount(1);
    }

    @Test
    void shouldCacheCollectionRelationship() throws Exception {
        // Create a customer with an order
        CustomerRequest customerRequest =
                new CustomerRequest("Collection", "Cache", "collection@example.com", "9876543210");

        // Create the customer first
        String customerJson = this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract customer ID
        Long customerId = objectMapper.readTree(customerJson).path("customerId").asLong();

        // Create an order for the customer
        OrderRequest orderRequest = new OrderRequest(
                customerId, "Collection Cache Order", List.of(new OrderItemRequest(BigDecimal.TEN, 5, "CACHE-ITEM")));

        SQLStatementCountValidator.reset();
        this.mockMvc
                .perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());

        // Multiple reads of the same customer to check orders collection
        SQLStatementCountValidator.reset();
        for (int i = 0; i < 5; i++) {
            this.mockMvc
                    .perform(get("/api/customers/{id}", customerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customerId", is(customerId), Long.class))
                    .andExpect(jsonPath("$.orders[0].name", is("Collection Cache Order")));
        }

        // Verify reduced SQL count due to collection caching
        SQLStatementCountValidator.assertSelectCount(2); // 2 selects for orders and order_items despite 5 API calls
    }

    @Test
    void shouldInvalidateCacheOnUpdate() throws Exception {
        // Create a test customer
        CustomerRequest customerRequest = new CustomerRequest("Update", "Cache", "update@example.com", "5555555555");

        String customerJson = this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long customerId = objectMapper.readTree(customerJson).path("customerId").asLong();

        // Read customer initially to cache it
        SQLStatementCountValidator.reset();
        this.mockMvc
                .perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Update")));

        SQLStatementCountValidator.assertSelectCount(1);
        SQLStatementCountValidator.reset();

        // Read again, should be cached
        this.mockMvc.perform(get("/api/customers/{id}", customerId)).andExpect(status().isOk());

        SQLStatementCountValidator.assertSelectCount(0); // No select, served from cache

        // Update the customer
        CustomerRequest updatedRequest =
                new CustomerRequest("UpdatedName", "Cache", "update@example.com", "5555555555");

        this.mockMvc
                .perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedRequest)))
                .andExpect(status().isOk());

        SQLStatementCountValidator.assertUpdateCount(1);
        SQLStatementCountValidator.assertSelectCount(0);
        SQLStatementCountValidator.assertTotalCount(1);
        SQLStatementCountValidator.reset();

        // Read again, cache should be updated after update
        this.mockMvc
                .perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("UpdatedName")));

        // Should not hit DB again because cache was updated
        SQLStatementCountValidator.assertSelectCount(0);
        SQLStatementCountValidator.assertTotalCount(0);
    }

    @Test
    void shouldInvalidateCacheOnDelete() throws Exception {
        // Create two test customers
        CustomerRequest customerRequest1 = new CustomerRequest("Delete", "Test", "delete1@example.com", "1111111111");
        CustomerRequest customerRequest2 = new CustomerRequest("Remain", "Test", "remain@example.com", "2222222222");

        // Create first customer (to be deleted)
        String customerJson1 = this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest1)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long customerId1 =
                objectMapper.readTree(customerJson1).path("customerId").asLong();

        // Create second customer (to remain)
        String customerJson2 = this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest2)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long customerId2 =
                objectMapper.readTree(customerJson2).path("customerId").asLong();

        // Cache both customers
        SQLStatementCountValidator.reset();

        this.mockMvc.perform(get("/api/customers/{id}", customerId1)).andExpect(status().isOk());
        this.mockMvc.perform(get("/api/customers/{id}", customerId2)).andExpect(status().isOk());

        SQLStatementCountValidator.assertSelectCount(2);
        SQLStatementCountValidator.reset();

        // Read again to verify cache is working
        this.mockMvc.perform(get("/api/customers/{id}", customerId1)).andExpect(status().isOk());
        this.mockMvc.perform(get("/api/customers/{id}", customerId2)).andExpect(status().isOk());

        SQLStatementCountValidator.assertSelectCount(0); // Both served from cache

        // Delete first customer
        this.mockMvc.perform(delete("/api/customers/{id}", customerId1)).andExpect(status().isOk());

        // Try to fetch deleted customer
        this.mockMvc.perform(get("/api/customers/{id}", customerId1)).andExpect(status().isNotFound());

        SQLStatementCountValidator.reset();

        // Second customer should still be cached
        this.mockMvc.perform(get("/api/customers/{id}", customerId2)).andExpect(status().isOk());

        SQLStatementCountValidator.assertSelectCount(0); // Still served from cache
    }

    @Test
    void shouldCacheOrderEntityWithItems() throws Exception {
        // Create a customer first
        CustomerRequest customerRequest = new CustomerRequest("Order", "Cache", "ordercache@example.com", "3333333333");

        String customerJson = this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long customerId = objectMapper.readTree(customerJson).path("customerId").asLong();

        // Create an order with multiple items
        OrderRequest orderRequest = new OrderRequest(
                customerId,
                "Cached Order",
                List.of(
                        new OrderItemRequest(BigDecimal.valueOf(10.99), 2, "ITEM-1"),
                        new OrderItemRequest(BigDecimal.valueOf(5.99), 3, "ITEM-2")));

        String orderJson = this.mockMvc
                .perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long orderId = objectMapper.readTree(orderJson).path("orderId").asLong();

        // Reset counter and read order multiple times
        SQLStatementCountValidator.reset();

        for (int i = 0; i < 5; i++) {
            this.mockMvc
                    .perform(get("/api/orders/{id}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId", is(orderId), Long.class))
                    .andExpect(jsonPath("$.orderItems.size()", is(2)));
        }

        // Verify fetch from cache
        SQLStatementCountValidator.assertSelectCount(1); // Just one hit to DB

        // Now update one of the order items
        Long orderItemId = objectMapper
                .readTree(orderJson)
                .path("orderItems")
                .get(0)
                .path("orderItemId")
                .asLong();

        OrderItemRequest updatedItem = new OrderItemRequest(BigDecimal.valueOf(15.99), 4, "ITEM-1-UPDATED");

        SQLStatementCountValidator.reset();

        this.mockMvc
                .perform(put("/api/order/items/{id}", orderItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price", is(15.99)))
                .andExpect(jsonPath("$.quantity", is(4)))
                .andExpect(jsonPath("$.itemCode", is("ITEM-1-UPDATED")));

        SQLStatementCountValidator.reset();

        // Order cache should be invalidated when child item is updated
        this.mockMvc
                .perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderItems[0].quantity", is(4)))
                .andExpect(jsonPath("$.orderItems[0].itemCode", is("ITEM-1-UPDATED")));

        // Verify DB was hit again due to cache invalidation
        SQLStatementCountValidator.assertSelectCount(1);
        SQLStatementCountValidator.assertTotalCount(1);
    }
}
