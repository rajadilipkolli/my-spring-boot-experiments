package com.example.multitenancy.schema.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.multitenancy.schema.common.AbstractIntegrationTest;
import com.example.multitenancy.schema.config.multitenancy.TenantIdentifierResolver;
import com.example.multitenancy.schema.domain.request.CustomerDto;
import com.example.multitenancy.schema.entities.Customer;
import com.example.multitenancy.schema.repositories.CustomerRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@DisplayName("Customer Controller Integration Tests")
class CustomerControllerIT extends AbstractIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TenantIdentifierResolver tenantIdentifierResolver;

    private static final String TENANT_1 = "test1";
    private static final String TENANT_2 = "test2";

    @BeforeEach
    void setUp() {
        // Clean up both tenant schemas
        cleanupTenant(TENANT_1);
        cleanupTenant(TENANT_2);
    }

    private void cleanupTenant(String tenantId) {
        tenantIdentifierResolver.setCurrentTenant(tenantId);
        customerRepository.deleteAll();
    }

    private List<Customer> createTestCustomersForTenant(String tenantId, String... customerNames) {
        tenantIdentifierResolver.setCurrentTenant(tenantId);
        List<Customer> customers = new ArrayList<>();
        for (String name : customerNames) {
            customers.add(new Customer().setName(name));
        }
        return customerRepository.saveAll(customers);
    }

    @Nested
    @DisplayName("Multi-Tenant Data Isolation Tests")
    class MultiTenantDataIsolationTests {

        @Test
        @DisplayName("Should maintain data isolation between tenants")
        void shouldMaintainDataIsolationBetweenTenants() throws Exception {
            // Create customers in tenant1
            createTestCustomersForTenant(TENANT_1, "Tenant1 Customer1", "Tenant1 Customer2", "Tenant1 Customer3");

            // Create customers in tenant2
            createTestCustomersForTenant(TENANT_2, "Tenant2 Customer1", "Tenant2 Customer2");

            // Verify tenant1 can only see its own customers
            mockMvc.perform(get("/api/customers").param("tenant", TENANT_1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(3)))
                    .andExpect(jsonPath("$[0].name", is("Tenant1 Customer1")))
                    .andExpect(jsonPath("$[1].name", is("Tenant1 Customer2")))
                    .andExpect(jsonPath("$[2].name", is("Tenant1 Customer3")));

            // Verify tenant2 can only see its own customers
            mockMvc.perform(get("/api/customers").param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(2)))
                    .andExpect(jsonPath("$[0].name", is("Tenant2 Customer1")))
                    .andExpect(jsonPath("$[1].name", is("Tenant2 Customer2")));
        }

        @Test
        @DisplayName("Should not allow tenant to access other tenant's customers by ID")
        void shouldNotAllowCrossTenantAccess() throws Exception {
            // Create customer in tenant1
            List<Customer> tenant1Customers = createTestCustomersForTenant(TENANT_1, "Tenant1 Customer");
            Customer tenant1Customer = tenant1Customers.get(0);

            // Create customer in tenant2
            List<Customer> tenant2Customers = createTestCustomersForTenant(TENANT_2, "Tenant2 Customer");
            Customer tenant2Customer = tenant2Customers.get(0);

            // Tenant1 should be able to access its own customer
            mockMvc.perform(get("/api/customers/{id}", tenant1Customer.getId()).param("tenant", TENANT_1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Tenant1 Customer")));

            // Tenant1 should NOT be able to access tenant2's customer
            mockMvc.perform(get("/api/customers/{id}", tenant2Customer.getId()).param("tenant", TENANT_1))
                    .andExpect(status().isNotFound());

            // Tenant2 should be able to access its own customer
            mockMvc.perform(get("/api/customers/{id}", tenant2Customer.getId()).param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Tenant2 Customer")));

            // Tenant2 should NOT be able to access tenant1's customer
            mockMvc.perform(get("/api/customers/{id}", tenant1Customer.getId()).param("tenant", TENANT_2))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Customer CRUD Operations for Tenant 1")
    class Tenant1CrudOperationsTests {

        @Test
        @DisplayName("Should fetch all customers for tenant1")
        void shouldFetchAllCustomersForTenant1() throws Exception {
            createTestCustomersForTenant(TENANT_1, "First Customer", "Second Customer", "Third Customer");

            mockMvc.perform(get("/api/customers").param("tenant", TENANT_1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(3)));
        }

        @Test
        @DisplayName("Should find customer by ID for tenant1")
        void shouldFindCustomerByIdForTenant1() throws Exception {
            List<Customer> customers = createTestCustomersForTenant(TENANT_1, "Test Customer");
            Customer customer = customers.get(0);

            mockMvc.perform(get("/api/customers/{id}", customer.getId()).param("tenant", TENANT_1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Test Customer")))
                    .andExpect(jsonPath("$.id", is(customer.getId().intValue())));
        }

        @Test
        @DisplayName("Should create new customer for tenant1")
        void shouldCreateNewCustomerForTenant1() throws Exception {
            CustomerDto customerDto = new CustomerDto("New Customer");

            mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customerDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is("New Customer")));

            // Verify customer was created in tenant1
            tenantIdentifierResolver.setCurrentTenant(TENANT_1);
            List<Customer> customers = customerRepository.findAll();
            assertThat(customers).hasSize(1);
            assertThat(customers.get(0).getName()).isEqualTo("New Customer");
        }

        @Test
        @DisplayName("Should update customer for tenant1")
        void shouldUpdateCustomerForTenant1() throws Exception {
            List<Customer> customers = createTestCustomersForTenant(TENANT_1, "Original Customer");
            Customer customer = customers.get(0);
            CustomerDto updateDto = new CustomerDto("Updated Customer");

            mockMvc.perform(put("/api/customers/{id}", customer.getId())
                            .param("tenant", TENANT_1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Updated Customer")));

            // Verify customer was updated in tenant1
            tenantIdentifierResolver.setCurrentTenant(TENANT_1);
            Customer updatedCustomer =
                    customerRepository.findById(customer.getId()).orElseThrow();
            assertThat(updatedCustomer.getName()).isEqualTo("Updated Customer");
        }

        @Test
        @DisplayName("Should delete customer for tenant1")
        void shouldDeleteCustomerForTenant1() throws Exception {
            List<Customer> customers = createTestCustomersForTenant(TENANT_1, "Customer to Delete");
            Customer customer = customers.get(0);

            mockMvc.perform(delete("/api/customers/{id}", customer.getId()).param("tenant", TENANT_1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Customer to Delete")));

            // Verify customer was deleted from tenant1
            tenantIdentifierResolver.setCurrentTenant(TENANT_1);
            assertThat(customerRepository.findById(customer.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Customer CRUD Operations for Tenant 2")
    class Tenant2CrudOperationsTests {

        @Test
        @DisplayName("Should fetch all customers for tenant2")
        void shouldFetchAllCustomersForTenant2() throws Exception {
            createTestCustomersForTenant(TENANT_2, "Tenant2 Customer1", "Tenant2 Customer2");

            mockMvc.perform(get("/api/customers").param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(2)));
        }

        @Test
        @DisplayName("Should find customer by ID for tenant2")
        void shouldFindCustomerByIdForTenant2() throws Exception {
            List<Customer> customers = createTestCustomersForTenant(TENANT_2, "Tenant2 Test Customer");
            Customer customer = customers.get(0);

            mockMvc.perform(get("/api/customers/{id}", customer.getId()).param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Tenant2 Test Customer")))
                    .andExpect(jsonPath("$.id", is(customer.getId().intValue())));
        }

        @Test
        @DisplayName("Should create new customer for tenant2")
        void shouldCreateNewCustomerForTenant2() throws Exception {
            CustomerDto customerDto = new CustomerDto("New Tenant2 Customer");

            mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customerDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is("New Tenant2 Customer")));

            // Verify customer was created in tenant2
            tenantIdentifierResolver.setCurrentTenant(TENANT_2);
            List<Customer> customers = customerRepository.findAll();
            assertThat(customers).hasSize(1);
            assertThat(customers.get(0).getName()).isEqualTo("New Tenant2 Customer");
        }

        @Test
        @DisplayName("Should update customer for tenant2")
        void shouldUpdateCustomerForTenant2() throws Exception {
            List<Customer> customers = createTestCustomersForTenant(TENANT_2, "Original Tenant2 Customer");
            Customer customer = customers.get(0);
            CustomerDto updateDto = new CustomerDto("Updated Tenant2 Customer");

            mockMvc.perform(put("/api/customers/{id}", customer.getId())
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Updated Tenant2 Customer")));

            // Verify customer was updated in tenant2
            tenantIdentifierResolver.setCurrentTenant(TENANT_2);
            Customer updatedCustomer =
                    customerRepository.findById(customer.getId()).orElseThrow();
            assertThat(updatedCustomer.getName()).isEqualTo("Updated Tenant2 Customer");
        }

        @Test
        @DisplayName("Should delete customer for tenant2")
        void shouldDeleteCustomerForTenant2() throws Exception {
            List<Customer> customers = createTestCustomersForTenant(TENANT_2, "Tenant2 Customer to Delete");
            Customer customer = customers.get(0);

            mockMvc.perform(delete("/api/customers/{id}", customer.getId()).param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Tenant2 Customer to Delete")));

            // Verify customer was deleted from tenant2
            tenantIdentifierResolver.setCurrentTenant(TENANT_2);
            assertThat(customerRepository.findById(customer.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Concurrent Operations Tests")
    class ConcurrentOperationsTests {

        @Test
        @DisplayName("Should handle concurrent operations on both tenants")
        void shouldHandleConcurrentOperationsOnBothTenants() throws Exception {
            // Create customers in both tenants
            createTestCustomersForTenant(TENANT_1, "T1 Customer1", "T1 Customer2");
            createTestCustomersForTenant(TENANT_2, "T2 Customer1", "T2 Customer2");

            // Perform operations on tenant1
            CustomerDto newT1Customer = new CustomerDto("New T1 Customer");
            mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newT1Customer)))
                    .andExpect(status().isCreated());

            // Perform operations on tenant2
            CustomerDto newT2Customer = new CustomerDto("New T2 Customer");
            mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newT2Customer)))
                    .andExpect(status().isCreated());

            // Verify tenant1 has 3 customers
            mockMvc.perform(get("/api/customers").param("tenant", TENANT_1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(3)));

            // Verify tenant2 has 3 customers
            mockMvc.perform(get("/api/customers").param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(3)));
        }

        @Test
        @DisplayName("Should handle operations when one tenant is empty")
        void shouldHandleOperationsWhenOneTenantIsEmpty() throws Exception {
            // Only create customers in tenant1
            createTestCustomersForTenant(TENANT_1, "Lonely Customer");

            // Verify tenant1 has customers
            mockMvc.perform(get("/api/customers").param("tenant", TENANT_1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(1)));

            // Verify tenant2 is empty
            mockMvc.perform(get("/api/customers").param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(0)));

            // Try to access non-existent customer in tenant2
            mockMvc.perform(get("/api/customers/{id}", 999L).param("tenant", TENANT_2))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Validation and Error Handling Tests")
    class ValidationAndErrorHandlingTests {

        @Test
        @DisplayName("Should return 400 when creating customer without name for tenant1")
        void shouldReturn400WhenCreatingCustomerWithoutNameForTenant1() throws Exception {
            CustomerDto invalidCustomer = new CustomerDto(null);

            mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidCustomer)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                    .andExpect(jsonPath("$.type", is("https://multitenancy-schema.com/errors/validation-error")))
                    .andExpect(jsonPath("$.title", is("Constraint Violation")))
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                    .andExpect(jsonPath("$.instance", is("/api/customers")))
                    .andExpect(jsonPath("$.properties.violations", hasSize(1)))
                    .andExpect(jsonPath("$.properties.violations[0].field", is("name")))
                    .andExpect(jsonPath("$.properties.violations[0].message", is("Name cannot be Blank")));
        }

        @Test
        @DisplayName("Should return 400 when creating customer without name for tenant2")
        void shouldReturn400WhenCreatingCustomerWithoutNameForTenant2() throws Exception {
            CustomerDto invalidCustomer = new CustomerDto("");

            mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidCustomer)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                    .andExpect(jsonPath("$.properties.violations[0].field", is("name")))
                    .andExpect(jsonPath("$.properties.violations[0].message", is("Name cannot be Blank")));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent customer in tenant1")
        void shouldReturn404WhenUpdatingNonExistentCustomerInTenant1() throws Exception {
            CustomerDto updateDto = new CustomerDto("Updated Name");

            mockMvc.perform(put("/api/customers/{id}", 999L)
                            .param("tenant", TENANT_1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent customer in tenant2")
        void shouldReturn404WhenUpdatingNonExistentCustomerInTenant2() throws Exception {
            CustomerDto updateDto = new CustomerDto("Updated Name");

            mockMvc.perform(put("/api/customers/{id}", 999L)
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent customer in both tenants")
        void shouldReturn404WhenDeletingNonExistentCustomerInBothTenants() throws Exception {
            // Test for tenant1
            mockMvc.perform(delete("/api/customers/{id}", 999L).param("tenant", TENANT_1))
                    .andExpect(status().isNotFound());

            // Test for tenant2
            mockMvc.perform(delete("/api/customers/{id}", 999L).param("tenant", TENANT_2))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when accessing without tenant parameter")
        void shouldReturn400WhenAccessingWithoutTenantParameter() throws Exception {
            mockMvc.perform(get("/api/customers")).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 403 when accessing with invalid tenant")
        void shouldReturn403WhenAccessingWithInvalidTenant() throws Exception {
            mockMvc.perform(get("/api/customers").param("tenant", "invalid")).andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Elaborate Tenant2 Specific Tests")
    class ElaborateTenant2SpecificTests {

        @Test
        @DisplayName("Should perform complete CRUD lifecycle for multiple customers in tenant2")
        void shouldPerformCompleteCrudLifecycleForMultipleCustomersInTenant2() throws Exception {
            // Phase 1: Create multiple customers in tenant2
            CustomerDto customer1 = new CustomerDto("Tenant2 Enterprise Customer");
            CustomerDto customer2 = new CustomerDto("Tenant2 Premium Customer");
            CustomerDto customer3 = new CustomerDto("Tenant2 Standard Customer");

            String response1 = mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customer1)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is("Tenant2 Enterprise Customer")))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String response2 = mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customer2)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is("Tenant2 Premium Customer")))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String response3 = mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customer3)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is("Tenant2 Standard Customer")))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Customer createdCustomer1 = objectMapper.readValue(response1, Customer.class);
            Customer createdCustomer2 = objectMapper.readValue(response2, Customer.class);
            Customer createdCustomer3 = objectMapper.readValue(response3, Customer.class);

            // Phase 2: Verify all customers exist and can be retrieved
            mockMvc.perform(get("/api/customers").param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(3)))
                    .andExpect(jsonPath("$[0].name", is("Tenant2 Enterprise Customer")))
                    .andExpect(jsonPath("$[1].name", is("Tenant2 Premium Customer")))
                    .andExpect(jsonPath("$[2].name", is("Tenant2 Standard Customer")));

            // Phase 3: Update each customer with different scenarios
            CustomerDto updatedCustomer1 = new CustomerDto("Tenant2 Enterprise Customer - VIP");
            mockMvc.perform(put("/api/customers/{id}", createdCustomer1.getId())
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedCustomer1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Tenant2 Enterprise Customer - VIP")));

            CustomerDto updatedCustomer2 = new CustomerDto("Tenant2 Premium Customer - Upgraded");
            mockMvc.perform(put("/api/customers/{id}", createdCustomer2.getId())
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedCustomer2)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Tenant2 Premium Customer - Upgraded")));

            // Phase 4: Verify updates were applied correctly
            mockMvc.perform(get("/api/customers/{id}", createdCustomer1.getId()).param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Tenant2 Enterprise Customer - VIP")));

            mockMvc.perform(get("/api/customers/{id}", createdCustomer2.getId()).param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Tenant2 Premium Customer - Upgraded")));

            // Phase 5: Delete one customer and verify
            mockMvc.perform(delete("/api/customers/{id}", createdCustomer3.getId())
                            .param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Tenant2 Standard Customer")));

            // Phase 6: Verify final state - only 2 customers remain
            mockMvc.perform(get("/api/customers").param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(2)));

            // Verify deleted customer cannot be accessed
            mockMvc.perform(get("/api/customers/{id}", createdCustomer3.getId()).param("tenant", TENANT_2))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should handle bulk operations efficiently for tenant2")
        void shouldHandleBulkOperationsEfficientlyForTenant2() throws Exception {
            List<String> customerNames = List.of(
                    "Tenant2 Bulk Customer 1",
                    "Tenant2 Bulk Customer 2",
                    "Tenant2 Bulk Customer 3",
                    "Tenant2 Bulk Customer 4",
                    "Tenant2 Bulk Customer 5",
                    "Tenant2 Bulk Customer 6",
                    "Tenant2 Bulk Customer 7",
                    "Tenant2 Bulk Customer 8",
                    "Tenant2 Bulk Customer 9",
                    "Tenant2 Bulk Customer 10");

            List<Customer> createdCustomers = new ArrayList<>();

            // Create 10 customers in rapid succession
            for (String name : customerNames) {
                CustomerDto customerDto = new CustomerDto(name);
                String response = mockMvc.perform(post("/api/customers")
                                .param("tenant", TENANT_2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(customerDto)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.name", is(name)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                createdCustomers.add(objectMapper.readValue(response, Customer.class));
            }

            // Verify all 10 customers exist
            mockMvc.perform(get("/api/customers").param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(10)));

            // Update every other customer (5 updates)
            for (int i = 0; i < createdCustomers.size(); i += 2) {
                Customer customer = createdCustomers.get(i);
                CustomerDto updateDto = new CustomerDto(customer.getName() + " - Updated");
                mockMvc.perform(put("/api/customers/{id}", customer.getId())
                                .param("tenant", TENANT_2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name", is(customer.getName() + " - Updated")));
            }

            // Delete the last 3 customers
            for (int i = 7; i < 10; i++) {
                Customer customer = createdCustomers.get(i);
                mockMvc.perform(delete("/api/customers/{id}", customer.getId()).param("tenant", TENANT_2))
                        .andExpect(status().isOk());
            }

            // Verify final state: 7 customers remain
            mockMvc.perform(get("/api/customers").param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(7)));
        }

        @Test
        @DisplayName("Should maintain data integrity during complex tenant2 operations")
        void shouldMaintainDataIntegrityDuringComplexTenant2Operations() throws Exception {
            // Create initial data set
            List<Customer> initialCustomers = createTestCustomersForTenant(
                    TENANT_2, "T2 Customer Alpha", "T2 Customer Beta", "T2 Customer Gamma");

            // Perform mixed operations: create, update, read, delete
            CustomerDto newCustomer = new CustomerDto("T2 Customer Delta");
            String newCustomerResponse = mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newCustomer)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Customer createdCustomer = objectMapper.readValue(newCustomerResponse, Customer.class);

            // Update first customer
            CustomerDto updateDto = new CustomerDto("T2 Customer Alpha - Modified");
            mockMvc.perform(put("/api/customers/{id}", initialCustomers.get(0).getId())
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk());

            // Delete second customer
            mockMvc.perform(delete(
                                    "/api/customers/{id}",
                                    initialCustomers.get(1).getId())
                            .param("tenant", TENANT_2))
                    .andExpect(status().isOk());

            // Verify data integrity
            mockMvc.perform(get("/api/customers").param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(3))); // 4 created - 1 deleted = 3

            // Verify specific customers exist with correct data
            mockMvc.perform(get("/api/customers/{id}", initialCustomers.get(0).getId())
                            .param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("T2 Customer Alpha - Modified")));

            mockMvc.perform(get("/api/customers/{id}", initialCustomers.get(2).getId())
                            .param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("T2 Customer Gamma")));

            mockMvc.perform(get("/api/customers/{id}", createdCustomer.getId()).param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("T2 Customer Delta")));

            // Verify deleted customer is not accessible
            mockMvc.perform(get("/api/customers/{id}", initialCustomers.get(1).getId())
                            .param("tenant", TENANT_2))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should handle edge cases specific to tenant2")
        void shouldHandleEdgeCasesSpecificToTenant2() throws Exception {
            // Test with very long customer name for tenant2
            String longName = "T2 " + "VeryLongCustomerName".repeat(10);
            CustomerDto longNameCustomer = new CustomerDto(longName);

            String longNameResponse = mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(longNameCustomer)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Customer createdLongNameCustomer = objectMapper.readValue(longNameResponse, Customer.class);

            // Test with special characters in customer name for tenant2
            String specialCharName = "T2 Customer with Special Chars: àáâãäåæçèéêë";
            CustomerDto specialCharCustomer = new CustomerDto(specialCharName);

            mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(specialCharCustomer)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is(specialCharName)));

            // Test updating to empty string (should fail validation)
            CustomerDto emptyNameUpdate = new CustomerDto("");
            mockMvc.perform(put("/api/customers/{id}", createdLongNameCustomer.getId())
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(emptyNameUpdate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.properties.violations[0].field", is("name")));

            // Test with whitespace-only name (should fail validation)
            CustomerDto whitespaceCustomer = new CustomerDto("   ");
            mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(whitespaceCustomer)))
                    .andExpect(status().isBadRequest());

            // Verify legitimate customers still exist
            mockMvc.perform(get("/api/customers").param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(2))); // longName + specialChar customers
        }
    }

    @Nested
    @DisplayName("Advanced Multi-Tenant Scenarios")
    class AdvancedMultiTenantScenarios {

        @Test
        @DisplayName("Should handle rapid tenant switching operations")
        void shouldHandleRapidTenantSwitchingOperations() throws Exception {
            // Create customers alternating between tenants
            CustomerDto t1Customer1 = new CustomerDto("T1 Rapid Customer 1");
            CustomerDto t2Customer1 = new CustomerDto("T2 Rapid Customer 1");
            CustomerDto t1Customer2 = new CustomerDto("T1 Rapid Customer 2");
            CustomerDto t2Customer2 = new CustomerDto("T2 Rapid Customer 2");

            // Rapid fire creation alternating tenants
            String t1c1Response = mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(t1Customer1)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String t2c1Response = mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(t2Customer1)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String t1c2Response = mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(t1Customer2)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String t2c2Response = mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(t2Customer2)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Customer t1c1 = objectMapper.readValue(t1c1Response, Customer.class);
            Customer t2c1 = objectMapper.readValue(t2c1Response, Customer.class);
            Customer t1c2 = objectMapper.readValue(t1c2Response, Customer.class);
            Customer t2c2 = objectMapper.readValue(t2c2Response, Customer.class);

            // Rapid fire operations alternating tenants
            CustomerDto updateDto1 = new CustomerDto("T1 Rapid Customer 1 - Updated");
            mockMvc.perform(put("/api/customers/{id}", t1c1.getId())
                            .param("tenant", TENANT_1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto1)))
                    .andExpect(status().isOk());

            CustomerDto updateDto2 = new CustomerDto("T2 Rapid Customer 1 - Updated");
            mockMvc.perform(put("/api/customers/{id}", t2c1.getId())
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto2)))
                    .andExpect(status().isOk());

            // Verify each tenant maintains its own data
            mockMvc.perform(get("/api/customers").param("tenant", TENANT_1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(2)))
                    .andExpect(jsonPath("$[0].name", is("T1 Rapid Customer 1 - Updated")))
                    .andExpect(jsonPath("$[1].name", is("T1 Rapid Customer 2")));

            mockMvc.perform(get("/api/customers").param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(2)))
                    .andExpect(jsonPath("$[0].name", is("T2 Rapid Customer 1 - Updated")))
                    .andExpect(jsonPath("$[1].name", is("T2 Rapid Customer 2")));

            // Verify individual customer access works correctly
            mockMvc.perform(get("/api/customers/{id}", t1c2.getId()).param("tenant", TENANT_1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("T1 Rapid Customer 2")));

            mockMvc.perform(get("/api/customers/{id}", t2c2.getId()).param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("T2 Rapid Customer 2")));
        }

        @Test
        @DisplayName("Should ensure complete isolation between tenant operations")
        void shouldEnsureCompleteIsolationBetweenTenantOperations() throws Exception {
            // Create large dataset in tenant1
            List<String> t1CustomerNames = List.of(
                    "T1 Enterprise Corp",
                    "T1 Tech Solutions",
                    "T1 Global Industries",
                    "T1 Innovation Hub",
                    "T1 Digital Services");

            List<Customer> t1Customers = new ArrayList<>();
            for (String name : t1CustomerNames) {
                CustomerDto dto = new CustomerDto(name);
                String response = mockMvc.perform(post("/api/customers")
                                .param("tenant", TENANT_1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
                t1Customers.add(objectMapper.readValue(response, Customer.class));
            }

            // Create different dataset in tenant2
            List<String> t2CustomerNames = List.of("T2 Startup Inc", "T2 Creative Agency", "T2 Consulting Group");

            List<Customer> t2Customers = new ArrayList<>();
            for (String name : t2CustomerNames) {
                CustomerDto dto = new CustomerDto(name);
                String response = mockMvc.perform(post("/api/customers")
                                .param("tenant", TENANT_2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
                t2Customers.add(objectMapper.readValue(response, Customer.class));
            }

            // Verify tenant1 cannot access tenant2 customer IDs
            for (Customer t2Customer : t2Customers) {
                mockMvc.perform(get("/api/customers/{id}", t2Customer.getId()).param("tenant", TENANT_1))
                        .andExpect(status().isNotFound());
            }

            // Verify tenant2 cannot access tenant1 customer IDs
            for (Customer t1Customer : t1Customers) {
                mockMvc.perform(get("/api/customers/{id}", t1Customer.getId()).param("tenant", TENANT_2))
                        .andExpect(status().isNotFound());
            }

            // Verify tenant2 cannot update tenant1 customers
            CustomerDto updateDto = new CustomerDto("Hacked Customer");
            for (Customer t1Customer : t1Customers) {
                mockMvc.perform(put("/api/customers/{id}", t1Customer.getId())
                                .param("tenant", TENANT_2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                        .andExpect(status().isNotFound());
            }

            // Verify tenant2 cannot delete tenant1 customers
            for (Customer t1Customer : t1Customers) {
                mockMvc.perform(delete("/api/customers/{id}", t1Customer.getId())
                                .param("tenant", TENANT_2))
                        .andExpect(status().isNotFound());
            }

            // Verify data integrity - tenant1 still has all its customers
            mockMvc.perform(get("/api/customers").param("tenant", TENANT_1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(5)));

            // Verify data integrity - tenant2 still has all its customers
            mockMvc.perform(get("/api/customers").param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(3)));
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should maintain separate ID sequences for different tenants")
        void shouldMaintainSeparateIdSequencesForDifferentTenants() throws Exception {
            // Create customers in tenant1
            CustomerDto customer1 = new CustomerDto("T1 Customer 1");
            CustomerDto customer2 = new CustomerDto("T1 Customer 2");

            String response1 = mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customer1)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String response2 = mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customer2)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            // Create customers in tenant2
            CustomerDto customer3 = new CustomerDto("T2 Customer 1");
            CustomerDto customer4 = new CustomerDto("T2 Customer 2");

            String response3 = mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customer3)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String response4 = mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customer4)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            // Parse responses and verify IDs are properly sequenced within each tenant
            Customer t1c1 = objectMapper.readValue(response1, Customer.class);
            Customer t1c2 = objectMapper.readValue(response2, Customer.class);
            Customer t2c1 = objectMapper.readValue(response3, Customer.class);
            Customer t2c2 = objectMapper.readValue(response4, Customer.class);

            // Each tenant should have its own ID sequence
            assertThat(t1c1.getId()).isNotNull();
            assertThat(t1c2.getId()).isNotNull();
            assertThat(t2c1.getId()).isNotNull();
            assertThat(t2c2.getId()).isNotNull();

            assertThat(t1c2.getId()).isGreaterThan(t1c1.getId());
            assertThat(t2c2.getId()).isGreaterThan(t2c1.getId());
        }

        @Test
        @DisplayName("Should allow same customer names in different tenants")
        void shouldAllowSameCustomerNamesInDifferentTenants() throws Exception {
            String sameName = "John Doe";
            CustomerDto customerDto = new CustomerDto(sameName);

            // Create customer with same name in tenant1
            mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customerDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is(sameName)));

            // Create customer with same name in tenant2
            mockMvc.perform(post("/api/customers")
                            .param("tenant", TENANT_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customerDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is(sameName)));

            // Verify both tenants have their own customer with the same name
            mockMvc.perform(get("/api/customers").param("tenant", TENANT_1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(1)))
                    .andExpect(jsonPath("$[0].name", is(sameName)));

            mockMvc.perform(get("/api/customers").param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(1)))
                    .andExpect(jsonPath("$[0].name", is(sameName)));
        }

        @Test
        @DisplayName("Should handle complex tenant2 business scenarios")
        void shouldHandleComplexTenant2BusinessScenarios() throws Exception {
            // Scenario: Tenant2 is migrating from another system and needs to import customers
            List<String> importedCustomerNames = List.of(
                    "Legacy Customer 001",
                    "Legacy Customer 002",
                    "Legacy Customer 003",
                    "Legacy Customer 004",
                    "Legacy Customer 005");

            List<Customer> importedCustomers = new ArrayList<>();

            // Import customers in batch
            for (String name : importedCustomerNames) {
                CustomerDto dto = new CustomerDto(name);
                String response = mockMvc.perform(post("/api/customers")
                                .param("tenant", TENANT_2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
                importedCustomers.add(objectMapper.readValue(response, Customer.class));
            }

            // Verify all imported customers are accessible
            mockMvc.perform(get("/api/customers").param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(5)));

            // Business scenario: Rename customers as part of rebranding
            for (int i = 0; i < importedCustomers.size(); i++) {
                Customer customer = importedCustomers.get(i);
                CustomerDto rebrandDto = new CustomerDto("Tenant2 Modern Customer " + (i + 1));

                mockMvc.perform(put("/api/customers/{id}", customer.getId())
                                .param("tenant", TENANT_2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(rebrandDto)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name", is("Tenant2 Modern Customer " + (i + 1))));
            }

            // Business scenario: Archive (delete) inactive customers
            Customer toArchive1 = importedCustomers.get(1);
            Customer toArchive2 = importedCustomers.get(3);

            mockMvc.perform(delete("/api/customers/{id}", toArchive1.getId()).param("tenant", TENANT_2))
                    .andExpect(status().isOk());

            mockMvc.perform(delete("/api/customers/{id}", toArchive2.getId()).param("tenant", TENANT_2))
                    .andExpect(status().isOk());

            // Verify final business state
            mockMvc.perform(get("/api/customers").param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", is(3)));

            // Verify remaining customers have correct names
            mockMvc.perform(get("/api/customers/{id}", importedCustomers.get(0).getId())
                            .param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Tenant2 Modern Customer 1")));

            mockMvc.perform(get("/api/customers/{id}", importedCustomers.get(2).getId())
                            .param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Tenant2 Modern Customer 3")));

            mockMvc.perform(get("/api/customers/{id}", importedCustomers.get(4).getId())
                            .param("tenant", TENANT_2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Tenant2 Modern Customer 5")));
        }
    }
}
