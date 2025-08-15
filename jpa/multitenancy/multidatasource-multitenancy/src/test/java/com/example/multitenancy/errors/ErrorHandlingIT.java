package com.example.multitenancy.errors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.multitenancy.common.AbstractIntegrationTest;
import com.example.multitenancy.primary.entities.PrimaryCustomer;
import com.example.multitenancy.primary.model.request.PrimaryCustomerRequest;
import com.example.multitenancy.secondary.entities.SecondaryCustomer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

@DisplayName("Error Handling and Exception Integration Tests")
class ErrorHandlingIT extends AbstractIntegrationTest {

    @BeforeEach
    void setUp() {
        // Clean up all data sources
        tenantIdentifierResolver.setCurrentTenant("primary");
        primaryCustomerRepository.deleteAllInBatch();

        tenantIdentifierResolver.setCurrentTenant("schema1");
        secondaryCustomerRepository.deleteAllInBatch();

        tenantIdentifierResolver.setCurrentTenant("schema2");
        secondaryCustomerRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("Tenant Header Validation Tests")
    class TenantHeaderValidationTests {

        @Test
        @DisplayName("Should return 400 when X-tenantId header is missing")
        void shouldReturn400WhenTenantIdHeaderIsMissing() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/customers/primary"))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.type").value("about:blank"))
                    .andExpect(jsonPath("$.title").value("Bad Request"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value("Required header 'X-tenantId' is not present."));

            mockMvc.perform(get("/api/customers/secondary"))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.type").value("about:blank"))
                    .andExpect(jsonPath("$.title").value("Bad Request"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value("Required header 'X-tenantId' is not present."));
        }

        @Test
        @DisplayName("Should return 403 when invalid tenant is provided")
        void shouldReturn403WhenInvalidTenantIsProvided() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/customers/primary").header("X-tenantId", "invalid_tenant"))
                    .andExpect(status().isForbidden())
                    .andExpect(header().string("Content-Type", "application/json"))
                    .andExpect(jsonPath("$.error").value("Unknown Database tenant"));

            mockMvc.perform(get("/api/customers/secondary").header("X-tenantId", "invalid_tenant"))
                    .andExpect(status().isForbidden())
                    .andExpect(header().string("Content-Type", "application/json"))
                    .andExpect(jsonPath("$.error").value("Unknown Database tenant"));
        }

        @Test
        @DisplayName("Should return 403 when empty tenant is provided")
        void shouldReturn403WhenEmptyTenantIsProvided() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/customers/primary").header("X-tenantId", ""))
                    .andExpect(status().isForbidden())
                    .andExpect(header().string("Content-Type", "application/json"))
                    .andExpect(jsonPath("$.error").value("Unknown Database tenant"));

            mockMvc.perform(get("/api/customers/secondary").header("X-tenantId", ""))
                    .andExpect(status().isForbidden())
                    .andExpect(header().string("Content-Type", "application/json"))
                    .andExpect(jsonPath("$.error").value("Unknown Database tenant"));
        }
    }

    @Nested
    @DisplayName("Validation Error Tests")
    class ValidationErrorTests {

        @Test
        @DisplayName("Should return validation errors for invalid primary customer")
        void shouldReturnValidationErrorsForInvalidPrimaryCustomer() throws Exception {
            // Given
            PrimaryCustomer invalidCustomer = new PrimaryCustomer().setText("");

            // When/Then
            mockMvc.perform(post("/api/customers/primary")
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidCustomer)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.type").value("about:blank"))
                    .andExpect(jsonPath("$.title").value("Constraint Violation"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value("Invalid request content."))
                    .andExpect(jsonPath("$.violations").isArray())
                    .andExpect(jsonPath("$.violations[0].field").value("text"))
                    .andExpect(jsonPath("$.violations[0].message").value("Text cannot be blank"));
        }

        @Test
        @DisplayName("Should return validation errors for invalid secondary customer")
        void shouldReturnValidationErrorsForInvalidSecondaryCustomer() throws Exception {
            // Given
            SecondaryCustomer invalidCustomer = new SecondaryCustomer().setName("");

            // When/Then
            mockMvc.perform(post("/api/customers/secondary")
                            .header("X-tenantId", "schema1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidCustomer)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.type").value("about:blank"))
                    .andExpect(jsonPath("$.title").value("Constraint Violation"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value("Invalid request content."))
                    .andExpect(jsonPath("$.violations").isArray())
                    .andExpect(jsonPath("$.violations[0].field").value("name"))
                    .andExpect(jsonPath("$.violations[0].message").value("Name cannot be blank"));
        }

        @Test
        @DisplayName("Should return validation errors for null customer data")
        void shouldReturnValidationErrorsForNullCustomerData() throws Exception {
            // Given
            PrimaryCustomer nullTextCustomer = new PrimaryCustomer().setText(null);

            // When/Then
            mockMvc.perform(post("/api/customers/primary")
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nullTextCustomer)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.violations[0].field").value("text"))
                    .andExpect(jsonPath("$.violations[0].message").value("Text cannot be blank"));

            // Given
            SecondaryCustomer nullNameCustomer = new SecondaryCustomer().setName(null);

            // When/Then
            mockMvc.perform(post("/api/customers/secondary")
                            .header("X-tenantId", "schema1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nullNameCustomer)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.violations[0].field").value("name"))
                    .andExpect(jsonPath("$.violations[0].message").value("Name cannot be blank"));
        }

        @Test
        @DisplayName("Should return validation errors for whitespace-only data")
        void shouldReturnValidationErrorsForWhitespaceOnlyData() throws Exception {
            // Given
            PrimaryCustomer whitespaceCustomer = new PrimaryCustomer().setText("   ");

            // When/Then
            mockMvc.perform(post("/api/customers/primary")
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(whitespaceCustomer)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.violations[0].field").value("text"))
                    .andExpect(jsonPath("$.violations[0].message").value("Text cannot be blank"));

            // Given
            SecondaryCustomer whitespaceSecondaryCustomer = new SecondaryCustomer().setName("   ");

            // When/Then
            mockMvc.perform(post("/api/customers/secondary")
                            .header("X-tenantId", "schema1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(whitespaceSecondaryCustomer)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.violations[0].field").value("name"))
                    .andExpect(jsonPath("$.violations[0].message").value("Name cannot be blank"));
        }
    }

    @Nested
    @DisplayName("Resource Not Found Tests")
    class ResourceNotFoundTests {

        @Test
        @DisplayName("Should return 404 for non-existent primary customer")
        void shouldReturn404ForNonExistentPrimaryCustomer() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/customers/primary/{id}", 99999L).header("X-tenantId", "primary"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 for non-existent secondary customer")
        void shouldReturn404ForNonExistentSecondaryCustomer() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/customers/secondary/{id}", 99999L).header("X-tenantId", "schema1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when trying to access customer from wrong tenant")
        void shouldReturn404WhenTryingToAccessCustomerFromWrongTenant() throws Exception {
            // Given - Create customer in schema1
            SecondaryCustomer customer = new SecondaryCustomer().setName("Test Customer");
            MvcResult createResult = mockMvc.perform(post("/api/customers/secondary")
                            .header("X-tenantId", "schema1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customer)))
                    .andExpect(status().isCreated())
                    .andReturn();

            SecondaryCustomer savedCustomer =
                    objectMapper.readValue(createResult.getResponse().getContentAsString(), SecondaryCustomer.class);

            // When/Then - Try to access from schema2
            mockMvc.perform(get("/api/customers/secondary/{id}", savedCustomer.getId())
                            .header("X-tenantId", "schema2"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent customer")
        void shouldReturn404WhenUpdatingNonExistentCustomer() throws Exception {
            // Given
            PrimaryCustomer nonExistentCustomer = new PrimaryCustomer().setText("Non-existent");
            nonExistentCustomer.setId(99999L);

            // When/Then
            mockMvc.perform(put("/api/customers/primary/{id}", 99999L)
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nonExistentCustomer)))
                    .andExpect(status().isNotFound());

            // Given
            SecondaryCustomer nonExistentSecondaryCustomer =
                    new SecondaryCustomer().setId(99999L).setName("Non-existent");

            // When/Then
            mockMvc.perform(put("/api/customers/secondary/{id}", 99999L)
                            .header("X-tenantId", "schema1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nonExistentSecondaryCustomer)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent customer")
        void shouldReturn404WhenDeletingNonExistentCustomer() throws Exception {
            // When/Then
            mockMvc.perform(delete("/api/customers/primary/{id}", 99999L).header("X-tenantId", "primary"))
                    .andExpect(status().isNotFound());

            mockMvc.perform(delete("/api/customers/secondary/{id}", 99999L).header("X-tenantId", "schema1"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Malformed Request Tests")
    class MalformedRequestTests {

        @Test
        @DisplayName("Should handle malformed JSON gracefully")
        void shouldHandleMalformedJsonGracefully() throws Exception {
            // Given
            String malformedJson = "{ \"text\": \"Test\", \"invalid\": }";

            // When/Then
            mockMvc.perform(post("/api/customers/primary")
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
                    .andExpect(status().isBadRequest());

            // Given
            String malformedSecondaryJson = "{ \"name\": \"Test\", \"invalid\": }";

            // When/Then
            mockMvc.perform(post("/api/customers/secondary")
                            .header("X-tenantId", "schema1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedSecondaryJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle missing content-type header")
        void shouldHandleMissingContentTypeHeader() throws Exception {
            // Given
            PrimaryCustomer customer = new PrimaryCustomer().setText("Test Customer");

            // When/Then
            mockMvc.perform(post("/api/customers/primary")
                            .header("X-tenantId", "primary")
                            .content(objectMapper.writeValueAsString(customer)))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("Should handle empty request body")
        void shouldHandleEmptyRequestBody() throws Exception {
            // When/Then
            mockMvc.perform(post("/api/customers/primary")
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(post("/api/customers/secondary")
                            .header("X-tenantId", "schema1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle invalid JSON structure")
        void shouldHandleInvalidJsonStructure() throws Exception {
            // Given
            String invalidJson = "{";

            // When/Then
            mockMvc.perform(post("/api/customers/primary")
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Concurrent Error Scenarios")
    class ConcurrentErrorScenarios {

        @Test
        @DisplayName("Should handle mixed valid and invalid requests")
        void shouldHandleMixedValidAndInvalidRequests() throws Exception {
            // Valid request
            PrimaryCustomer validCustomer = new PrimaryCustomer().setText("Valid Customer");
            mockMvc.perform(post("/api/customers/primary")
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCustomer)))
                    .andExpect(status().isCreated());

            // Invalid request
            PrimaryCustomer invalidCustomer = new PrimaryCustomer().setText("");
            mockMvc.perform(post("/api/customers/primary")
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidCustomer)))
                    .andExpect(status().isBadRequest());

            // Another valid request
            PrimaryCustomer anotherValidCustomer = new PrimaryCustomer().setText("Another Valid Customer");
            mockMvc.perform(post("/api/customers/primary")
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(anotherValidCustomer)))
                    .andExpect(status().isCreated());

            // Verify valid customers were saved
            mockMvc.perform(get("/api/customers/primary").header("X-tenantId", "primary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(2));
        }

        @Test
        @DisplayName("Should handle errors during tenant switching")
        void shouldHandleErrorsDuringTenantSwitching() throws Exception {
            // Valid operation in schema1
            SecondaryCustomer customer1 = new SecondaryCustomer().setName("Schema1 Customer");
            mockMvc.perform(post("/api/customers/secondary")
                            .header("X-tenantId", "schema1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customer1)))
                    .andExpect(status().isCreated());

            // Invalid tenant
            SecondaryCustomer customer2 = new SecondaryCustomer().setName("Invalid Tenant Customer");
            mockMvc.perform(post("/api/customers/secondary")
                            .header("X-tenantId", "invalid_tenant")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customer2)))
                    .andExpect(status().isForbidden());

            // Valid operation in schema2
            SecondaryCustomer customer3 = new SecondaryCustomer().setName("Schema2 Customer");
            mockMvc.perform(post("/api/customers/secondary")
                            .header("X-tenantId", "schema2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customer3)))
                    .andExpect(status().isCreated());

            // Verify data integrity
            mockMvc.perform(get("/api/customers/secondary").header("X-tenantId", "schema1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(1));

            mockMvc.perform(get("/api/customers/secondary").header("X-tenantId", "schema2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(1));
        }
    }

    @Nested
    @DisplayName("Edge Case Error Tests")
    class EdgeCaseErrorTests {

        @Test
        @DisplayName("Should handle special characters and encoding issues")
        void shouldHandleSpecialCharactersAndEncodingIssues() throws Exception {
            // Given
            String specialCharsText = "Customer with émojis 🎉 and spéciál çhars \u0000 \uFFFF";
            PrimaryCustomerRequest specialCustomer = new PrimaryCustomerRequest(specialCharsText);

            // When/Then - Should handle gracefully
            MvcResult result = mockMvc.perform(post("/api/customers/primary")
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(specialCustomer)))
                    .andReturn();

            // Should be 201 (created) but not 5xx
            int status = result.getResponse().getStatus();
            assertThat(status).isIn(201);
        }

        @Test
        @DisplayName("Should handle null and undefined values gracefully")
        void shouldHandleNullAndUndefinedValuesGracefully() throws Exception {
            // Given
            String jsonWithNulls = """
                    {"text": null}
                    """;

            // When/Then
            mockMvc.perform(post("/api/customers/primary")
                            .header("X-tenantId", "primary")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonWithNulls))
                    .andExpect(status().isBadRequest());

            // Given
            String secondaryJsonWithNulls = """
                {"name": null}
                """;

            // When/Then
            mockMvc.perform(post("/api/customers/secondary")
                            .header("X-tenantId", "schema1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(secondaryJsonWithNulls))
                    .andExpect(status().isBadRequest());
        }
    }
}
