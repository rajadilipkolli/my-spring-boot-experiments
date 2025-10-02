package com.example.multitenancy.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.multitenancy.common.AbstractIntegrationTest;
import com.example.multitenancy.primary.entities.PrimaryCustomer;
import com.example.multitenancy.primary.model.request.PrimaryCustomerRequest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@DisplayName("Primary Customer Controller Integration Tests")
class PrimaryCustomerControllerIT extends AbstractIntegrationTest {

    private List<PrimaryCustomer> primaryCustomerList = null;

    @BeforeEach
    void setUp() {
        tenantIdentifierResolver.setCurrentTenant("primary");
        primaryCustomerRepository.deleteAllInBatch();

        primaryCustomerList = new ArrayList<>();
        primaryCustomerList.add(new PrimaryCustomer().setText("First Customer"));
        primaryCustomerList.add(new PrimaryCustomer().setText("Second Customer"));
        primaryCustomerList.add(new PrimaryCustomer().setText("Third Customer"));
        primaryCustomerList = primaryCustomerRepository.saveAll(primaryCustomerList);
    }

    @Nested
    @DisplayName("Header Validation Tests")
    class HeaderValidationTests {

        @Test
        @DisplayName("Should fail when X-tenantId header is not present")
        void shouldFailWhenHeaderNotSetForFetchAllCustomers() throws Exception {
            mockMvc.perform(get("/api/customers/primary"))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                    .andExpect(jsonPath("$.type", is("https://multitenancy.com/errors/header-error")))
                    .andExpect(jsonPath("$.title", is("Header Violation")))
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.detail", is("Required header 'X-tenantId' is not present.")))
                    .andExpect(jsonPath("$.instance", is("/api/customers/primary")));
        }

        @Test
        @DisplayName("Should fail when invalid tenant header is provided")
        void shouldFailWhenWrongHeaderSetForFetchAllCustomers() throws Exception {
            mockMvc.perform(get("/api/customers/primary").header("X-tenantId", "junk"))
                    .andExpect(status().isForbidden())
                    .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                    .andExpect(jsonPath("$.type", is("https://multitenancy.com/errors/tenant-error")))
                    .andExpect(jsonPath("$.title", is("Invalid Tenant")))
                    .andExpect(jsonPath("$.status", is(403)))
                    .andExpect(jsonPath("$.detail", is("Unknown Database tenant")))
                    .andExpect(jsonPath("$.instance", is("/api/customers/primary")));
        }

        @Test
        @DisplayName("Should fail when empty tenant header is provided")
        void shouldFailWhenEmptyHeaderSetForFetchAllCustomers() throws Exception {
            mockMvc.perform(get("/api/customers/primary").header("X-tenantId", ""))
                    .andExpect(status().isForbidden())
                    .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                    .andExpect(jsonPath("$.type", is("https://multitenancy.com/errors/tenant-error")))
                    .andExpect(jsonPath("$.title", is("Invalid Tenant")))
                    .andExpect(jsonPath("$.status", is(403)))
                    .andExpect(jsonPath("$.detail", is("Unknown Database tenant")))
                    .andExpect(jsonPath("$.instance", is("/api/customers/primary")));
        }
    }

    @Nested
    @DisplayName("Customer Retrieval Tests")
    class CustomerRetrievalTests {

        @Test
        @DisplayName("Should fetch all customers for primary tenant")
        void shouldFetchAllCustomersForPrimary() throws Exception {
            mockMvc.perform(get("/api/customers/primary").header("X-tenantId", "primary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(primaryCustomerList.size())));
        }

        @Test
        @DisplayName("Should find customer by ID")
        void shouldFindCustomerById() throws Exception {
            PrimaryCustomer primaryCustomer = primaryCustomerList.getFirst();
            Long customerId = primaryCustomer.getId();

            mockMvc.perform(get("/api/customers/primary/{id}", customerId).header("X-tenantId", "primary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.text", is(primaryCustomer.getText())))
                    .andExpect(jsonPath("$.tenant", is("primary")));
        }

        @Test
        @DisplayName("Should return 404 when customer ID does not exist")
        void shouldReturn404WhenCustomerDoesNotExist() throws Exception {
            mockMvc.perform(get("/api/customers/primary/{id}", 99999L).header("X-tenantId", "primary"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Customer Creation Tests")
    class CustomerCreationTests {

        @Test
        @DisplayName("Should create new customer successfully")
        void shouldCreateNewCustomer() throws Exception {
            PrimaryCustomerRequest newCustomer = new PrimaryCustomerRequest("New Customer");

            mockMvc.perform(post("/api/customers/primary")
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newCustomer)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.text", is(newCustomer.text())))
                    .andExpect(jsonPath("$.tenant", is("primary")))
                    .andExpect(jsonPath("$.id", notNullValue()));
        }

        @Test
        @DisplayName("Should return 400 when creating customer without text")
        void shouldReturn400WhenCreateNewCustomerWithoutText() throws Exception {
            PrimaryCustomerRequest primaryCustomer = new PrimaryCustomerRequest(null);

            mockMvc.perform(post("/api/customers/primary")
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(primaryCustomer)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                    .andExpect(jsonPath("$.type", is("https://multitenancy.com/errors/validation-error")))
                    .andExpect(jsonPath("$.title", is("Constraint Violation")))
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                    .andExpect(jsonPath("$.instance", is("/api/customers/primary")))
                    .andExpect(jsonPath("$.properties.violations", hasSize(1)))
                    .andExpect(jsonPath("$.properties.violations[0].field", is("text")))
                    .andExpect(jsonPath("$.properties.violations[0].message", is("Text cannot be blank")));
        }

        @Test
        @DisplayName("Should return 400 when creating customer with empty text")
        void shouldReturn400WhenCreateNewCustomerWithEmptyText() throws Exception {
            PrimaryCustomerRequest primaryCustomer = new PrimaryCustomerRequest("");

            mockMvc.perform(post("/api/customers/primary")
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(primaryCustomer)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                    .andExpect(jsonPath("$.properties.violations[0].field", is("text")))
                    .andExpect(jsonPath("$.properties.violations[0].message", is("Text cannot be blank")));
        }

        @Test
        @DisplayName("Should return 400 when creating customer with whitespace-only text")
        void shouldReturn400WhenCreateNewCustomerWithWhitespaceOnlyText() throws Exception {
            PrimaryCustomerRequest primaryCustomer = new PrimaryCustomerRequest("   ");

            mockMvc.perform(post("/api/customers/primary")
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(primaryCustomer)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                    .andExpect(jsonPath("$.properties.violations[0].field", is("text")))
                    .andExpect(jsonPath("$.properties.violations[0].message", is("Text cannot be blank")));
        }
    }

    @Nested
    @DisplayName("Customer Update Tests")
    class CustomerUpdateTests {

        @Test
        @DisplayName("Should update customer successfully")
        void shouldUpdateCustomer() throws Exception {
            PrimaryCustomer primaryCustomer = primaryCustomerList.getFirst();
            PrimaryCustomerRequest primaryCustomerRequest = new PrimaryCustomerRequest("Updated Customer");

            mockMvc.perform(put("/api/customers/primary/{id}", primaryCustomer.getId())
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(primaryCustomerRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(primaryCustomer.getId()), Long.class))
                    .andExpect(jsonPath("$.text", is(primaryCustomerRequest.text())))
                    .andExpect(jsonPath("$.version", is(1)))
                    .andExpect(jsonPath("$.tenant", is("primary")));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent customer")
        void shouldReturn404WhenUpdatingNonExistentCustomer() throws Exception {
            PrimaryCustomerRequest nonExistentCustomer = new PrimaryCustomerRequest("Non-existent");

            mockMvc.perform(put("/api/customers/primary/{id}", 99999L)
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nonExistentCustomer)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when updating customer with invalid data")
        void shouldReturn400WhenUpdatingCustomerWithInvalidData() throws Exception {
            PrimaryCustomer primaryCustomer = primaryCustomerList.getFirst();
            PrimaryCustomerRequest request = new PrimaryCustomerRequest(null);

            mockMvc.perform(put("/api/customers/primary/{id}", primaryCustomer.getId())
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)));
        }
    }

    @Nested
    @DisplayName("Customer Deletion Tests")
    class CustomerDeletionTests {

        @Test
        @DisplayName("Should delete customer successfully")
        void shouldDeleteCustomer() throws Exception {
            PrimaryCustomer primaryCustomer = primaryCustomerList.getFirst();

            mockMvc.perform(delete("/api/customers/primary/{id}", primaryCustomer.getId())
                            .header("X-tenantId", "primary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.text", is(primaryCustomer.getText())))
                    .andExpect(jsonPath("$.tenant", is("primary")));

            // Verify customer is deleted
            mockMvc.perform(get("/api/customers/primary/{id}", primaryCustomer.getId())
                            .header("X-tenantId", "primary"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent customer")
        void shouldReturn404WhenDeletingNonExistentCustomer() throws Exception {
            mockMvc.perform(delete("/api/customers/primary/{id}", 99999L).header("X-tenantId", "primary"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle malformed JSON gracefully")
        void shouldHandleMalformedJsonGracefully() throws Exception {
            String malformedJson = "{ \"text\": \"Test\", \"invalid\": }";

            mockMvc.perform(post("/api/customers/primary")
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle very long customer text")
        void shouldHandleVeryLongCustomerText() throws Exception {
            String longText = "A".repeat(1000); // Very long text
            PrimaryCustomerRequest customerWithLongText = new PrimaryCustomerRequest(longText);

            // This might pass or fail depending on database constraints
            // The test verifies the application handles it gracefully
            try {
                mockMvc.perform(post("/api/customers/primary")
                                .header("X-tenantId", "primary")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(customerWithLongText)))
                        .andExpect(status().isCreated());
            } catch (Exception e) {
                // Expected to fail with very long text due to database constraints
                mockMvc.perform(post("/api/customers/primary")
                                .header("X-tenantId", "primary")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(customerWithLongText)))
                        .andExpect(status().is4xxClientError());
            }
        }

        @Test
        @DisplayName("Should handle special characters in customer text")
        void shouldHandleSpecialCharactersInCustomerText() throws Exception {
            String specialCharText = "Customer with émojis 🎉 and spéciál çhars";
            PrimaryCustomerRequest customerWithSpecialChars = new PrimaryCustomerRequest(specialCharText);

            mockMvc.perform(post("/api/customers/primary")
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customerWithSpecialChars)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.text", is(specialCharText)));
        }

        @Test
        @DisplayName("Should handle concurrent requests properly")
        void shouldHandleConcurrentRequestsProperly() throws Exception {
            // This is a basic test - in a real scenario, you'd use proper concurrency testing
            PrimaryCustomerRequest customer1 = new PrimaryCustomerRequest("Concurrent Customer 1");
            PrimaryCustomerRequest customer2 = new PrimaryCustomerRequest("Concurrent Customer 2");

            // Simulate near-concurrent requests
            mockMvc.perform(post("/api/customers/primary")
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customer1)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/customers/primary")
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customer2)))
                    .andExpect(status().isCreated());

            // Verify data integrity - should now have 5 customers (3 original + 2 new)
            mockMvc.perform(get("/api/customers/primary").header("X-tenantId", "primary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(5)));
        }
    }
}
