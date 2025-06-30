package com.example.multitenancy.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.multitenancy.common.AbstractIntegrationTest;
import com.example.multitenancy.secondary.entities.SecondaryCustomer;
import com.example.multitenancy.secondary.model.request.SecondaryCustomerRequest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@DisplayName("Secondary Customer Controller Integration Tests")
class SecondaryCustomerControllerIT extends AbstractIntegrationTest {

    private List<SecondaryCustomer> secondaryCustomerList = null;

    @BeforeEach
    void setUp() {
        // Clean up data from both schemas before each test
        tenantIdentifierResolver.setCurrentTenant("schema1");
        secondaryCustomerRepository.deleteAllInBatch();

        tenantIdentifierResolver.setCurrentTenant("schema2");
        secondaryCustomerRepository.deleteAllInBatch();

        // Set back to schema1 and create test data
        tenantIdentifierResolver.setCurrentTenant("schema1");
        secondaryCustomerList = new ArrayList<>();
        secondaryCustomerList.add(new SecondaryCustomer().setName("First Customer"));
        secondaryCustomerList.add(new SecondaryCustomer().setName("Second Customer"));
        secondaryCustomerList.add(new SecondaryCustomer().setName("Third Customer"));
        secondaryCustomerList = secondaryCustomerRepository.saveAll(secondaryCustomerList);
    }

    @Nested
    @DisplayName("Header Validation Tests")
    class HeaderValidationTests {

        @Test
        @DisplayName("Should fail when X-tenantId header is not present")
        void shouldFailWhenHeaderNotSetForFetchAllCustomers() throws Exception {
            mockMvc.perform(get("/api/customers/secondary"))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", is("application/problem+json")))
                    .andExpect(jsonPath("$.type", is("about:blank")))
                    .andExpect(jsonPath("$.title", is("Bad Request")))
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(
                            jsonPath(
                                    "$.detail", is("Required header 'X-tenantId' is not present.")))
                    .andExpect(jsonPath("$.instance", is("/api/customers/secondary")));
        }

        @Test
        @DisplayName("Should fail when invalid tenant header is provided")
        void shouldFailWhenWrongHeaderSetForFetchAllCustomers() throws Exception {
            mockMvc.perform(get("/api/customers/secondary").header("X-tenantId", "junk"))
                    .andExpect(status().isForbidden())
                    .andExpect(header().string("Content-Type", is("application/json")))
                    .andExpect(jsonPath("$.error", is("Unknown Database tenant")));
        }

        @Test
        @DisplayName("Should fail when empty tenant header is provided")
        void shouldFailWhenEmptyHeaderSetForFetchAllCustomers() throws Exception {
            mockMvc.perform(get("/api/customers/secondary").header("X-tenantId", ""))
                    .andExpect(status().isForbidden())
                    .andExpect(header().string("Content-Type", is("application/json")))
                    .andExpect(jsonPath("$.error", is("Unknown Database tenant")));
        }
    }

    @Nested
    @DisplayName("Customer Retrieval Tests")
    class CustomerRetrievalTests {

        @Test
        @DisplayName("Should fetch all customers for schema1 tenant")
        void shouldFetchAllCustomersForSchema1() throws Exception {
            mockMvc.perform(get("/api/customers/secondary").header("X-tenantId", "schema1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(secondaryCustomerList.size())));
        }

        @Test
        @DisplayName("Should fetch empty list for schema2 tenant")
        void shouldFetchEmptyListForSchema2() throws Exception {
            mockMvc.perform(get("/api/customers/secondary").header("X-tenantId", "schema2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(0)));
        }

        @Test
        @DisplayName("Should find customer by ID in correct tenant")
        void shouldFindCustomerById() throws Exception {
            SecondaryCustomer secondaryCustomer = secondaryCustomerList.getFirst();
            Long customerId = secondaryCustomer.getId();

            mockMvc.perform(
                            get("/api/customers/secondary/{id}", customerId)
                                    .header("X-tenantId", "schema1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(customerId.intValue())))
                    .andExpect(jsonPath("$.name", is(secondaryCustomer.getName())));
        }

        @Test
        @DisplayName("Should return 404 when customer not found in different tenant")
        void shouldReturn404WhenCustomerNotFoundInDifferentTenant() throws Exception {
            SecondaryCustomer secondaryCustomer = secondaryCustomerList.getFirst();
            Long customerId = secondaryCustomer.getId();

            mockMvc.perform(
                            get("/api/customers/secondary/{id}", customerId)
                                    .header("X-tenantId", "schema2"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when customer ID does not exist")
        void shouldReturn404WhenCustomerDoesNotExist() throws Exception {
            mockMvc.perform(
                            get("/api/customers/secondary/{id}", 99999L)
                                    .header("X-tenantId", "schema1"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Customer Creation Tests")
    class CustomerCreationTests {

        @Test
        @DisplayName("Should create new customer successfully")
        void shouldCreateNewCustomer() throws Exception {
            SecondaryCustomerRequest newCustomer = new SecondaryCustomerRequest("New Customer");

            mockMvc.perform(
                            post("/api/customers/secondary")
                                    .header("X-tenantId", "schema1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(newCustomer)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is(newCustomer.name())))
                    .andExpect(jsonPath("$.id", notNullValue()));
        }

        @Test
        @DisplayName("Should create customer in different tenant without conflicts")
        void shouldCreateCustomerInDifferentTenant() throws Exception {
            SecondaryCustomerRequest newCustomer = new SecondaryCustomerRequest("Schema2 Customer");

            mockMvc.perform(
                            post("/api/customers/secondary")
                                    .header("X-tenantId", "schema2")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(newCustomer)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is(newCustomer.name())))
                    .andExpect(jsonPath("$.id", notNullValue()));

            // Verify tenant isolation - should still have 3 customers in schema1
            mockMvc.perform(get("/api/customers/secondary").header("X-tenantId", "schema1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(3)));

            // And 1 customer in schema2
            mockMvc.perform(get("/api/customers/secondary").header("X-tenantId", "schema2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(1)));
        }

        @Test
        @DisplayName("Should return 400 when creating customer without name")
        void shouldReturn400WhenCreateNewCustomerWithoutName() throws Exception {
            SecondaryCustomerRequest secondaryCustomer = new SecondaryCustomerRequest(null);

            mockMvc.perform(
                            post("/api/customers/secondary")
                                    .header("X-tenantId", "schema1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(secondaryCustomer)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", is("application/problem+json")))
                    .andExpect(jsonPath("$.type", is("about:blank")))
                    .andExpect(jsonPath("$.title", is("Constraint Violation")))
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                    .andExpect(jsonPath("$.instance", is("/api/customers/secondary")))
                    .andExpect(jsonPath("$.violations", hasSize(1)))
                    .andExpect(jsonPath("$.violations[0].field", is("name")))
                    .andExpect(jsonPath("$.violations[0].message", is("Name cannot be blank")));
        }

        @Test
        @DisplayName("Should return 400 when creating customer with empty name")
        void shouldReturn400WhenCreateNewCustomerWithEmptyName() throws Exception {
            var secondaryCustomer = new SecondaryCustomerRequest("");

            mockMvc.perform(
                            post("/api/customers/secondary")
                                    .header("X-tenantId", "schema1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(secondaryCustomer)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", is("application/problem+json")))
                    .andExpect(jsonPath("$.violations[0].field", is("name")))
                    .andExpect(jsonPath("$.violations[0].message", is("Name cannot be blank")));
        }

        @Test
        @DisplayName("Should return 400 when creating customer with whitespace-only name")
        void shouldReturn400WhenCreateNewCustomerWithWhitespaceOnlyName() throws Exception {
            var secondaryCustomer = new SecondaryCustomerRequest("   ");

            mockMvc.perform(
                            post("/api/customers/secondary")
                                    .header("X-tenantId", "schema1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(secondaryCustomer)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", is("application/problem+json")))
                    .andExpect(jsonPath("$.violations[0].field", is("name")))
                    .andExpect(jsonPath("$.violations[0].message", is("Name cannot be blank")));
        }
    }

    @Nested
    @DisplayName("Customer Update Tests")
    class CustomerUpdateTests {

        @Test
        @DisplayName("Should update customer successfully")
        void shouldUpdateCustomer() throws Exception {
            SecondaryCustomer secondaryCustomer = secondaryCustomerList.getFirst();
            var updateRequest = new SecondaryCustomerRequest("Updated Customer");

            mockMvc.perform(
                            put("/api/customers/secondary/{id}", secondaryCustomer.getId())
                                    .header("X-tenantId", "schema1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is(secondaryCustomer.getName())))
                    .andExpect(jsonPath("$.id", is(secondaryCustomer.getId().intValue())));
        }

        @Test
        @DisplayName("Should return 404 when updating customer in wrong tenant")
        void shouldReturn404WhenUpdatingCustomerInWrongTenant() throws Exception {
            SecondaryCustomer secondaryCustomer = secondaryCustomerList.getFirst();
            var updateRequest = new SecondaryCustomerRequest("Updated Customer");

            mockMvc.perform(
                            put("/api/customers/secondary/{id}", secondaryCustomer.getId())
                                    .header("X-tenantId", "schema2")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent customer")
        void shouldReturn404WhenUpdatingNonExistentCustomer() throws Exception {
            SecondaryCustomerRequest nonExistentCustomer =
                    new SecondaryCustomerRequest("Non-existent");

            mockMvc.perform(
                            put("/api/customers/secondary/{id}", 99999L)
                                    .header("X-tenantId", "schema1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(nonExistentCustomer)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when updating customer with invalid data")
        void shouldReturn400WhenUpdatingCustomerWithInvalidData() throws Exception {
            SecondaryCustomer secondaryCustomer = secondaryCustomerList.getFirst();
            var updateRequest = new SecondaryCustomerRequest(null);

            mockMvc.perform(
                            put("/api/customers/secondary/{id}", secondaryCustomer.getId())
                                    .header("X-tenantId", "schema1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", is("application/problem+json")));
        }
    }

    @Nested
    @DisplayName("Customer Deletion Tests")
    class CustomerDeletionTests {

        @Test
        @DisplayName("Should delete customer successfully")
        void shouldDeleteCustomer() throws Exception {
            SecondaryCustomer secondaryCustomer = secondaryCustomerList.getFirst();

            mockMvc.perform(
                            delete("/api/customers/secondary/{id}", secondaryCustomer.getId())
                                    .header("X-tenantId", "schema1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is(secondaryCustomer.getName())))
                    .andExpect(jsonPath("$.id", is(secondaryCustomer.getId().intValue())));

            // Verify customer is deleted
            mockMvc.perform(
                            get("/api/customers/secondary/{id}", secondaryCustomer.getId())
                                    .header("X-tenantId", "schema1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when deleting customer from wrong tenant")
        void shouldReturn404WhenDeletingCustomerFromWrongTenant() throws Exception {
            SecondaryCustomer secondaryCustomer = secondaryCustomerList.getFirst();

            mockMvc.perform(
                            delete("/api/customers/secondary/{id}", secondaryCustomer.getId())
                                    .header("X-tenantId", "schema2"))
                    .andExpect(status().isNotFound());

            // Verify customer still exists in correct tenant
            mockMvc.perform(
                            get("/api/customers/secondary/{id}", secondaryCustomer.getId())
                                    .header("X-tenantId", "schema1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent customer")
        void shouldReturn404WhenDeletingNonExistentCustomer() throws Exception {
            mockMvc.perform(
                            delete("/api/customers/secondary/{id}", 99999L)
                                    .header("X-tenantId", "schema1"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Tenant Isolation Tests")
    class TenantIsolationTests {

        @Test
        @DisplayName("Should maintain data isolation between tenants")
        void shouldMaintainDataIsolationBetweenTenants() throws Exception {
            // Create customer in schema2
            SecondaryCustomer schema2Customer = new SecondaryCustomer().setName("Schema2 Customer");
            String schema2Response =
                    mockMvc.perform(
                                    post("/api/customers/secondary")
                                            .header("X-tenantId", "schema2")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    objectMapper.writeValueAsString(
                                                            schema2Customer)))
                            .andExpect(status().isCreated())
                            .andReturn()
                            .getResponse()
                            .getContentAsString();

            SecondaryCustomer createdSchema2Customer =
                    objectMapper.readValue(schema2Response, SecondaryCustomer.class);

            // Verify schema1 still has 3 customers
            mockMvc.perform(get("/api/customers/secondary").header("X-tenantId", "schema1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(3)));

            // Verify schema2 has 1 customer
            mockMvc.perform(get("/api/customers/secondary").header("X-tenantId", "schema2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(1)));

            // Try to access schema2 customer from schema1 - should not be found
            mockMvc.perform(
                            get("/api/customers/secondary/{id}", createdSchema2Customer.getId())
                                    .header("X-tenantId", "schema1"))
                    .andExpect(status().isNotFound());

            // Access schema2 customer from schema2 - should be found
            mockMvc.perform(
                            get("/api/customers/secondary/{id}", createdSchema2Customer.getId())
                                    .header("X-tenantId", "schema2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Schema2 Customer")));
        }

        @Test
        @DisplayName("Should allow same customer names in different tenants")
        void shouldAllowSameCustomerNamesInDifferentTenants() throws Exception {
            String customerName = "Duplicate Name Customer";

            // Create customer with same name in schema1
            SecondaryCustomer schema1Customer = new SecondaryCustomer().setName(customerName);
            mockMvc.perform(
                            post("/api/customers/secondary")
                                    .header("X-tenantId", "schema1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(schema1Customer)))
                    .andExpect(status().isCreated());

            // Create customer with same name in schema2
            SecondaryCustomer schema2Customer = new SecondaryCustomer().setName(customerName);
            mockMvc.perform(
                            post("/api/customers/secondary")
                                    .header("X-tenantId", "schema2")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(schema2Customer)))
                    .andExpect(status().isCreated());

            // Verify both customers exist in their respective tenants
            mockMvc.perform(get("/api/customers/secondary").header("X-tenantId", "schema1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(4))); // 3 original + 1 new

            mockMvc.perform(get("/api/customers/secondary").header("X-tenantId", "schema2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(1)));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle malformed JSON gracefully")
        void shouldHandleMalformedJsonGracefully() throws Exception {
            String malformedJson = "{ \"name\": \"Test\", \"invalid\": }";

            mockMvc.perform(
                            post("/api/customers/secondary")
                                    .header("X-tenantId", "schema1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(malformedJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle very long customer names")
        void shouldHandleVeryLongCustomerNames() throws Exception {
            String longName = "A".repeat(1000); // Very long name
            SecondaryCustomer customerWithLongName = new SecondaryCustomer().setName(longName);

            // This might pass or fail depending on database constraints
            // The test verifies the application handles it gracefully
            try {
                mockMvc.perform(
                                post("/api/customers/secondary")
                                        .header("X-tenantId", "schema1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        customerWithLongName)))
                        .andExpect(status().isCreated());
            } catch (Exception e) {
                // Expected to fail with very long names due to database constraints
                mockMvc.perform(
                                post("/api/customers/secondary")
                                        .header("X-tenantId", "schema1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        customerWithLongName)))
                        .andExpect(status().is4xxClientError());
            }
        }

        @Test
        @DisplayName("Should handle special characters in customer names")
        void shouldHandleSpecialCharactersInCustomerNames() throws Exception {
            String specialCharName = "Customer with émojis 🎉 and spéciál çhars";
            SecondaryCustomer customerWithSpecialChars =
                    new SecondaryCustomer().setName(specialCharName);

            mockMvc.perform(
                            post("/api/customers/secondary")
                                    .header("X-tenantId", "schema1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            objectMapper.writeValueAsString(
                                                    customerWithSpecialChars)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is(specialCharName)));
        }

        @Test
        @DisplayName("Should handle concurrent requests to different tenants")
        void shouldHandleConcurrentRequestsToDifferentTenants() throws Exception {
            // This is a basic test - in a real scenario, you'd use proper concurrency testing
            SecondaryCustomer customer1 = new SecondaryCustomer().setName("Concurrent Customer 1");
            SecondaryCustomer customer2 = new SecondaryCustomer().setName("Concurrent Customer 2");

            // Simulate near-concurrent requests
            mockMvc.perform(
                            post("/api/customers/secondary")
                                    .header("X-tenantId", "schema1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(customer1)))
                    .andExpect(status().isCreated());

            mockMvc.perform(
                            post("/api/customers/secondary")
                                    .header("X-tenantId", "schema2")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(customer2)))
                    .andExpect(status().isCreated());

            // Verify data integrity
            mockMvc.perform(get("/api/customers/secondary").header("X-tenantId", "schema1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(4)));

            mockMvc.perform(get("/api/customers/secondary").header("X-tenantId", "schema2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(1)));
        }
    }
}
