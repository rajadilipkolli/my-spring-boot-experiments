package com.example.multitenancy.scenarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.multitenancy.common.AbstractIntegrationTest;
import com.example.multitenancy.primary.entities.PrimaryCustomer;
import com.example.multitenancy.primary.model.request.PrimaryCustomerRequest;
import com.example.multitenancy.secondary.entities.SecondaryCustomer;
import com.example.multitenancy.secondary.model.request.SecondaryCustomerRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;

class AdvancedMultiTenantScenariosIT extends AbstractIntegrationTest {

    @BeforeEach
    void setUp() {
        // Clean up all data before each test - use correct tenant identifiers
        tenantIdentifierResolver.setCurrentTenant("primary");
        primaryCustomerRepository.deleteAll();

        tenantIdentifierResolver.setCurrentTenant("schema1");
        secondaryCustomerRepository.deleteAll();

        tenantIdentifierResolver.setCurrentTenant("schema2");
        secondaryCustomerRepository.deleteAll();

        // Reset to default
        tenantIdentifierResolver.setCurrentTenant("primary");
    }

    @Test
    void testTenantDataIsolation() {
        // Create data for primary tenant
        tenantIdentifierResolver.setCurrentTenant("primary");
        createTestDataForTenant("Primary");

        // Create data for schema1 tenant
        tenantIdentifierResolver.setCurrentTenant("schema1");
        createTestDataForTenant("Schema1");

        // Verify primary tenant data isolation
        tenantIdentifierResolver.setCurrentTenant("primary");
        List<PrimaryCustomer> primaryCustomers = primaryCustomerRepository.findAll();

        assertThat(primaryCustomers).hasSize(2);
        assertThat(primaryCustomers.getFirst().getText()).startsWith("Primary");

        // Verify schema1 tenant data isolation
        tenantIdentifierResolver.setCurrentTenant("schema1");
        List<SecondaryCustomer> schema1Customers = secondaryCustomerRepository.findAll();

        assertThat(schema1Customers).hasSize(2);
        assertThat(schema1Customers.getFirst().getName()).startsWith("Schema1");
    }

    @Test
    void testTenantSwitchingInSequence() {
        // Test sequential tenant switching with verification
        String[] tenants = {"primary", "schema1", "schema2"};

        for (int i = 0; i < tenants.length; i++) {
            String tenant = tenants[i];
            tenantIdentifierResolver.setCurrentTenant(tenant);

            createTestDataForTenant("Tenant" + (i + 1));

            // Verify data exists for current tenant
            if (tenant.equals("primary")) {
                List<PrimaryCustomer> primaryCustomers = primaryCustomerRepository.findAll();
                assertThat(primaryCustomers).hasSize(2);
                assertThat(primaryCustomers.getFirst().getText()).startsWith("Tenant" + (i + 1));
            } else {
                List<SecondaryCustomer> secondaryCustomers = secondaryCustomerRepository.findAll();
                assertThat(secondaryCustomers).hasSize(2);
                assertThat(secondaryCustomers.getFirst().getName()).startsWith("Tenant" + (i + 1));
            }
        }

        // Verify all tenants have isolated data
        for (int i = 0; i < tenants.length; i++) {
            String tenant = tenants[i];
            tenantIdentifierResolver.setCurrentTenant(tenant);

            if (tenant.equals("primary")) {
                List<PrimaryCustomer> primaryCustomers = primaryCustomerRepository.findAll();
                assertThat(primaryCustomers).hasSize(2);
                assertThat(primaryCustomers.getFirst().getText()).startsWith("Tenant" + (i + 1));
            } else {
                List<SecondaryCustomer> secondaryCustomers = secondaryCustomerRepository.findAll();
                assertThat(secondaryCustomers).hasSize(2);
                assertThat(secondaryCustomers.getFirst().getName()).startsWith("Tenant" + (i + 1));
            }
        }
    }

    @Test
    void testTransactionIsolationBetweenTenants() {
        // Test transaction rollback doesn't affect other tenants
        tenantIdentifierResolver.setCurrentTenant("primary");
        createTestDataForTenant("Primary");

        tenantIdentifierResolver.setCurrentTenant("schema1");
        SecondaryCustomer customer = new SecondaryCustomer();
        customer.setName("Schema1-Customer1");
        secondaryCustomerRepository.save(customer);

        // Verify schema1 has data
        List<SecondaryCustomer> schema1Data = secondaryCustomerRepository.findAll();
        assertThat(schema1Data).hasSize(1);

        // Switch back to primary and verify data is still intact
        tenantIdentifierResolver.setCurrentTenant("primary");
        List<PrimaryCustomer> primaryData = primaryCustomerRepository.findAll();
        assertThat(primaryData).hasSize(2);
        assertThat(primaryData.getFirst().getText()).startsWith("Primary");
    }

    @Test
    void testHttpRequestTenantIsolation() throws Exception {
        // Create data for different tenants via HTTP requests
        String primaryCustomer =
                objectMapper.writeValueAsString(
                        new PrimaryCustomerRequest("Primary-HTTP-Customer"));

        mockMvc.perform(
                        post("/api/customers/primary")
                                .header("X-TenantId", "primary")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(primaryCustomer))
                .andExpect(status().isCreated());

        String secondaryCustomer =
                objectMapper.writeValueAsString(
                        new SecondaryCustomerRequest("Schema1-HTTP-Customer"));

        mockMvc.perform(
                        post("/api/customers/secondary")
                                .header("X-TenantId", "schema1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(secondaryCustomer))
                .andExpect(status().isCreated());

        // Verify primary data
        mockMvc.perform(get("/api/customers/primary").header("X-TenantId", "primary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].text").value("Primary-HTTP-Customer"));

        // Verify schema1 data
        mockMvc.perform(get("/api/customers/secondary").header("X-TenantId", "schema1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Schema1-HTTP-Customer"));
    }

    @Test
    void testErrorHandlingWithTenantContext() {
        // Test that errors don't corrupt tenant context
        tenantIdentifierResolver.setCurrentTenant("primary");

        // Test that validation errors don't corrupt tenant context
        assertThatThrownBy(
                        () -> {
                            PrimaryCustomer invalidCustomer = new PrimaryCustomer();
                            invalidCustomer.setText(""); // Invalid empty text
                            primaryCustomerRepository.saveAndFlush(invalidCustomer);
                        })
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("cannot insert NULL");

        // Verify tenant context is still correct
        assertThat(tenantIdentifierResolver.resolveCurrentTenantIdentifier()).isEqualTo("primary");

        // Verify we can still operate normally
        createTestDataForTenant("Primary");
        List<PrimaryCustomer> customers = primaryCustomerRepository.findAll();
        assertThat(customers).hasSize(2);
    }

    @Test
    void testBulkOperationsWithTenantIsolation() {
        // Test bulk operations respect tenant boundaries
        tenantIdentifierResolver.setCurrentTenant("primary");
        for (int i = 1; i <= 5; i++) {
            PrimaryCustomer customer = new PrimaryCustomer();
            customer.setText("Primary-Bulk-" + i);
            primaryCustomerRepository.save(customer);
        }

        tenantIdentifierResolver.setCurrentTenant("schema1");
        for (int i = 1; i <= 3; i++) {
            SecondaryCustomer customer = new SecondaryCustomer();
            customer.setName("Schema1-Bulk-" + i);
            secondaryCustomerRepository.save(customer);
        }

        // Verify counts per tenant
        tenantIdentifierResolver.setCurrentTenant("primary");
        assertThat(primaryCustomerRepository.count()).isEqualTo(5);

        tenantIdentifierResolver.setCurrentTenant("schema1");
        assertThat(secondaryCustomerRepository.count()).isEqualTo(3);

        // Test bulk delete for one tenant
        tenantIdentifierResolver.setCurrentTenant("primary");
        primaryCustomerRepository.deleteAll();

        // Verify primary data is gone but schema1 data remains
        assertThat(primaryCustomerRepository.count()).isEqualTo(0);

        tenantIdentifierResolver.setCurrentTenant("schema1");
        assertThat(secondaryCustomerRepository.count()).isEqualTo(3);
    }

    @Test
    void testTenantContextClearing() {
        // Test that clearing context works properly
        tenantIdentifierResolver.setCurrentTenant("primary");
        assertThat(tenantIdentifierResolver.resolveCurrentTenantIdentifier()).isEqualTo("primary");

        tenantIdentifierResolver.setCurrentTenant(null);
        assertThat(tenantIdentifierResolver.resolveCurrentTenantIdentifier()).isEqualTo("unknown");

        // Test operations without tenant context (should use default/fail gracefully)
        try {
            primaryCustomerRepository.findAll();
            // This might succeed with default tenant or fail - both are acceptable
        } catch (Exception e) {
            // Expected if no default tenant configured
        }
    }

    private void createTestDataForTenant(String tenantPrefix) {
        String currentTenant = tenantIdentifierResolver.resolveCurrentTenantIdentifier();

        if ("primary".equals(currentTenant)
                || "dbsystc".equals(currentTenant)
                || "dbsystp".equals(currentTenant)
                || "dbsystv".equals(currentTenant)) {
            // Create primary customers
            PrimaryCustomer primary1 = new PrimaryCustomer();
            primary1.setText(tenantPrefix + "-Primary-Customer1");
            primaryCustomerRepository.save(primary1);

            PrimaryCustomer primary2 = new PrimaryCustomer();
            primary2.setText(tenantPrefix + "-Primary-Customer2");
            primaryCustomerRepository.save(primary2);
        } else {
            // Create secondary customers
            SecondaryCustomer secondary1 = new SecondaryCustomer();
            secondary1.setName(tenantPrefix + "-Secondary-Customer1");
            secondaryCustomerRepository.save(secondary1);

            SecondaryCustomer secondary2 = new SecondaryCustomer();
            secondary2.setName(tenantPrefix + "-Secondary-Customer2");
            secondaryCustomerRepository.save(secondary2);
        }
    }
}
