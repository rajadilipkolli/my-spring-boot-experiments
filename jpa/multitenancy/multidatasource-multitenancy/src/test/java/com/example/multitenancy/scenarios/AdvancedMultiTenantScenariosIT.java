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
import com.example.multitenancy.utils.DatabaseType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
    void tenantDataIsolation() {
        // Create data for primary tenant
        tenantIdentifierResolver.setCurrentTenant("primary");
        createTestDataForTenant("Primary");

        // Create data for schema1 tenant
        tenantIdentifierResolver.setCurrentTenant("schema1");
        createTestDataForTenant("Schema1");

        // Verify primary tenant data isolation
        tenantIdentifierResolver.setCurrentTenant("primary");
        List<PrimaryCustomer> primaryCustomers = primaryCustomerRepository.findAll();

        assertThat(primaryCustomers).isNotEmpty().hasSize(2);
        assertThat(primaryCustomers.getFirst().getText()).startsWith("Primary");

        // Verify schema1 tenant data isolation
        tenantIdentifierResolver.setCurrentTenant("schema1");
        List<SecondaryCustomer> schema1Customers = secondaryCustomerRepository.findAll();

        assertThat(schema1Customers).isNotEmpty().hasSize(2);
        assertThat(schema1Customers.getFirst().getName()).startsWith("Schema1");
    }

    @Test
    void tenantSwitchingInSequence() {
        // Test sequential tenant switching with verification
        String[] tenants = {"primary", "schema1", "schema2"};

        for (int i = 0; i < tenants.length; i++) {
            String tenant = tenants[i];
            tenantIdentifierResolver.setCurrentTenant(tenant);

            createTestDataForTenant("Tenant" + (i + 1));

            // Verify data exists for current tenant
            if (tenant.equals("primary")) {
                List<PrimaryCustomer> primaryCustomers = primaryCustomerRepository.findAll();
                assertThat(primaryCustomers).isNotEmpty().hasSize(2);
                assertThat(primaryCustomers.getFirst().getText()).startsWith("Tenant" + (i + 1));
            } else {
                List<SecondaryCustomer> secondaryCustomers = secondaryCustomerRepository.findAll();
                assertThat(secondaryCustomers).isNotEmpty().hasSize(2);
                assertThat(secondaryCustomers.getFirst().getName()).startsWith("Tenant" + (i + 1));
            }
        }
    }

    @Test
    void dataIsolationBetweenTenants() {
        // Test that data operations in one tenant don't affect other tenants
        tenantIdentifierResolver.setCurrentTenant("primary");
        createTestDataForTenant("Primary");

        // Verify primary data exists before schema1 operations
        List<PrimaryCustomer> primaryDataBefore = primaryCustomerRepository.findAll();
        assertThat(primaryDataBefore).hasSize(2);

        tenantIdentifierResolver.setCurrentTenant("schema1");
        // Save initial customer in schema1
        SecondaryCustomer customer = new SecondaryCustomer();
        customer.setName("Schema1-Customer1");
        secondaryCustomerRepository.save(customer);

        // Add more data to schema1
        SecondaryCustomer customer2 = new SecondaryCustomer();
        customer2.setName("Schema1-Customer2");
        secondaryCustomerRepository.save(customer2);

        SecondaryCustomer customer3 = new SecondaryCustomer();
        customer3.setName("Schema1-Customer3");
        secondaryCustomerRepository.save(customer3);

        // Verify schema1 has 3 customers
        List<SecondaryCustomer> schema1Data = secondaryCustomerRepository.findAll();
        assertThat(schema1Data).hasSize(3);

        // Switch back to primary and verify data is still intact
        tenantIdentifierResolver.setCurrentTenant("primary");
        List<PrimaryCustomer> primaryDataAfter = primaryCustomerRepository.findAll();
        assertThat(primaryDataAfter).hasSize(2).isNotEmpty();
        assertThat(primaryDataAfter.getFirst().getText()).startsWith("Primary");

        // Verify primary data is exactly the same as before the schema1 operations
        assertThat(primaryDataAfter).hasSameSizeAs(primaryDataBefore);

        // Switch to schema2 and verify it has no data from schema1 operations
        tenantIdentifierResolver.setCurrentTenant("schema2");
        List<SecondaryCustomer> schema2Data = secondaryCustomerRepository.findAll();
        assertThat(schema2Data).isEmpty();
    }

    @Test
    void httpRequestTenantIsolation() throws Exception {
        // Create data for different tenants via HTTP requests
        String primaryCustomer = objectMapper.writeValueAsString(new PrimaryCustomerRequest("Primary-HTTP-Customer"));

        mockMvc.perform(post("/api/customers/primary")
                        .header("X-TenantId", "primary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(primaryCustomer))
                .andExpect(status().isCreated());

        String secondaryCustomer =
                objectMapper.writeValueAsString(new SecondaryCustomerRequest("Schema1-HTTP-Customer"));

        mockMvc.perform(post("/api/customers/secondary")
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
    void errorHandlingWithTenantContext() {
        // Test that errors don't corrupt tenant context
        tenantIdentifierResolver.setCurrentTenant("primary");

        // Test that validation errors don't corrupt tenant context
        assertThatThrownBy(() -> {
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
    void bulkOperationsWithTenantIsolation() {
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
        assertThat(primaryCustomerRepository.count()).isZero();

        tenantIdentifierResolver.setCurrentTenant("schema1");
        assertThat(secondaryCustomerRepository.count()).isEqualTo(3);
    }

    @Test
    void tenantContextClearing() {
        // Test that clearing context works properly
        tenantIdentifierResolver.setCurrentTenant("primary");
        assertThat(tenantIdentifierResolver.resolveCurrentTenantIdentifier()).isEqualTo("primary");

        tenantIdentifierResolver.setCurrentTenant(null);
        assertThat(tenantIdentifierResolver.resolveCurrentTenantIdentifier()).isEqualTo("unknown");

        // Test operations without tenant context (should fail gracefully with known exception)
        assertThatThrownBy(() -> primaryCustomerRepository.findAll())
                .isInstanceOf(Exception.class)
                .hasRootCauseMessage("Cannot determine target DataSource for lookup key [unknown]");
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
            primary1.setTenant(currentTenant); // Set the tenant to match current context
            primaryCustomerRepository.save(primary1);

            PrimaryCustomer primary2 = new PrimaryCustomer();
            primary2.setText(tenantPrefix + "-Primary-Customer2");
            primary2.setTenant(currentTenant); // Set the tenant to match current context
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

    /**
     * Comprehensive concurrent testing scenarios to validate tenant switching works correctly
     * across multiple threads with different tenant combinations from DatabaseType enum.
     */
    @Nested
    class ConcurrentTenantSwitchingTests {

        private DatabaseType[] getValidTenants() {
            return new DatabaseType[] {
                DatabaseType.PRIMARY,
                DatabaseType.SCHEMA1,
                DatabaseType.SCHEMA2,
                DatabaseType.DBSYSTC,
                DatabaseType.DBSYSTP
            };
        }

        @Test
        void shouldHandleConcurrentTenantSwitchingWithAllTenants() throws Exception {
            // Test concurrent operations across only properly configured tenants
            // Only use tenants that have proper database setup
            DatabaseType[] allTenants = getValidTenants();
            int threadsPerTenant = 3;
            int totalThreads = allTenants.length * threadsPerTenant;

            ExecutorService executorService = Executors.newFixedThreadPool(totalThreads);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch completionLatch = new CountDownLatch(totalThreads);

            // Track results per tenant
            ConcurrentHashMap<String, AtomicInteger> successCounts = new ConcurrentHashMap<>();
            ConcurrentHashMap<String, List<String>> errors = new ConcurrentHashMap<>();

            // Initialize tracking maps
            for (DatabaseType tenant : allTenants) {
                successCounts.put(tenant.getSchemaName(), new AtomicInteger(0));
                errors.put(tenant.getSchemaName(), new ArrayList<>());
            }

            try {
                // Create tasks for each tenant
                List<CompletableFuture<Void>> futures = new ArrayList<>();

                for (DatabaseType tenant : allTenants) {
                    for (int i = 0; i < threadsPerTenant; i++) {
                        final int threadIndex = i;
                        final String tenantName = tenant.getSchemaName();

                        CompletableFuture<Void> future = CompletableFuture.runAsync(
                                () -> {
                                    try {
                                        // Wait for all threads to start simultaneously
                                        startLatch.await();

                                        // Set tenant context for this thread
                                        tenantIdentifierResolver.setCurrentTenant(tenantName);

                                        // Verify tenant context is correctly set
                                        String currentTenant =
                                                tenantIdentifierResolver.resolveCurrentTenantIdentifier();
                                        assertThat(currentTenant).isEqualTo(tenantName);

                                        // Perform tenant-specific operations
                                        performTenantSpecificOperations(tenantName, threadIndex);

                                        // Verify data isolation
                                        verifyTenantDataIsolation(tenantName, threadIndex);

                                        successCounts.get(tenantName).incrementAndGet();

                                    } catch (Exception e) {
                                        errors.get(tenantName).add("Thread " + threadIndex + ": " + e.getMessage());
                                        e.printStackTrace();
                                    } finally {
                                        completionLatch.countDown();
                                    }
                                },
                                executorService);

                        futures.add(future);
                    }
                }

                // Start all threads simultaneously
                startLatch.countDown();

                // Wait for all threads to complete (with timeout)
                boolean completed = completionLatch.await(60, TimeUnit.SECONDS);
                assertThat(completed).isTrue();

                // Wait for all futures to complete
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .get(30, TimeUnit.SECONDS);

                // Verify results
                for (DatabaseType tenant : allTenants) {
                    String tenantName = tenant.getSchemaName();
                    int successCount = successCounts.get(tenantName).get();
                    List<String> tenantErrors = errors.get(tenantName);

                    // Assert that all threads for this tenant succeeded
                    assertThat(tenantErrors)
                            .as("Errors for tenant " + tenantName + ": " + tenantErrors)
                            .isEmpty();
                    assertThat(successCount)
                            .as("Success count for tenant " + tenantName)
                            .isEqualTo(threadsPerTenant);
                }

            } finally {
                executorService.shutdown();
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            }
        }

        @Test
        void shouldHandleRapidTenantSwitchingInSingleThread() {
            // Test rapid tenant switching within a single thread
            // Only use tenants that have proper database setup
            DatabaseType[] tenants = getValidTenants();
            int iterationsPerTenant = 5;

            for (int iteration = 0; iteration < iterationsPerTenant; iteration++) {
                for (DatabaseType tenant : tenants) {
                    String tenantName = tenant.getSchemaName();

                    // Switch tenant
                    tenantIdentifierResolver.setCurrentTenant(tenantName);

                    // Verify switch was successful
                    assertThat(tenantIdentifierResolver.resolveCurrentTenantIdentifier())
                            .isEqualTo(tenantName);

                    // Create and verify data
                    createAndVerifyTenantData(tenantName, iteration);
                }
            }

            // Final verification - check that data exists for all tenants
            verifyAllTenantsHaveData();
        }

        @Test
        void shouldMaintainTenantIsolationUnderStress() throws Exception {
            // Stress test with high concurrency and frequent tenant switches
            int numberOfThreads = 20;
            int operationsPerThread = 10;
            ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
            CountDownLatch completionLatch = new CountDownLatch(numberOfThreads);

            AtomicInteger totalOperations = new AtomicInteger(0);
            ConcurrentHashMap<String, AtomicInteger> tenantOperationCounts = new ConcurrentHashMap<>();
            List<String> globalErrors = new ArrayList<>();

            try {
                for (int threadId = 0; threadId < numberOfThreads; threadId++) {
                    final int currentThreadId = threadId;

                    executorService.submit(() -> {
                        try {
                            DatabaseType[] tenants = getValidTenants();

                            for (int op = 0; op < operationsPerThread; op++) {
                                // Randomly select a tenant for this operation
                                DatabaseType randomTenant = tenants[op % tenants.length];
                                String tenantName = randomTenant.getSchemaName();

                                // Switch to tenant
                                tenantIdentifierResolver.setCurrentTenant(tenantName);

                                // Verify correct tenant is set
                                String currentTenant = tenantIdentifierResolver.resolveCurrentTenantIdentifier();
                                if (!tenantName.equals(currentTenant)) {
                                    synchronized (globalErrors) {
                                        globalErrors.add("Thread "
                                                + currentThreadId
                                                + " expected tenant "
                                                + tenantName
                                                + " but got "
                                                + currentTenant);
                                    }
                                    continue;
                                }

                                // Perform operation
                                String dataId = "thread-" + currentThreadId + "-op-" + op;
                                createTenantSpecificData(tenantName, dataId);

                                // Verify data was created in correct tenant
                                verifyDataInTenant(tenantName, dataId);

                                // Track operations per tenant
                                tenantOperationCounts
                                        .computeIfAbsent(tenantName, k -> new AtomicInteger(0))
                                        .incrementAndGet();
                                totalOperations.incrementAndGet();

                                // Small delay to increase chance of race conditions
                                Thread.sleep(1);
                            }
                        } catch (Exception e) {
                            synchronized (globalErrors) {
                                globalErrors.add("Thread " + currentThreadId + " error: " + e.getMessage());
                            }
                            e.printStackTrace();
                        } finally {
                            completionLatch.countDown();
                        }
                    });
                }

                // Wait for completion
                boolean completed = completionLatch.await(120, TimeUnit.SECONDS);
                assertThat(completed).isTrue();

                // Verify no errors occurred
                assertThat(globalErrors).as("Global errors: " + globalErrors).isEmpty();

                // Verify operations were distributed across tenants
                assertThat(totalOperations.get()).isEqualTo(numberOfThreads * operationsPerThread);
                assertThat(tenantOperationCounts).isNotEmpty();

                // Each tenant should have received some operations
                DatabaseType[] validTenants = getValidTenants();
                for (DatabaseType tenant : validTenants) {
                    String tenantName = tenant.getSchemaName();
                    assertThat(tenantOperationCounts.get(tenantName))
                            .as("Tenant " + tenantName + " should have received operations")
                            .isNotNull()
                            .satisfies(count -> assertThat(count.get()).isPositive());
                }

            } finally {
                executorService.shutdown();
                if (!executorService.awaitTermination(15, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            }
        }

        private void performTenantSpecificOperations(String tenantName, int threadIndex) {
            String uniqueId = tenantName + "-thread-" + threadIndex + "-" + System.currentTimeMillis();
            createTenantSpecificData(tenantName, uniqueId);
        }

        private void createTenantSpecificData(String tenantName, String uniqueId) {
            if (isPrimaryTenant(tenantName)) {
                PrimaryCustomer customer = new PrimaryCustomer();
                customer.setText("Concurrent-" + uniqueId);
                customer.setTenant(tenantName); // Set the tenant to match current context
                primaryCustomerRepository.save(customer);
            } else {
                SecondaryCustomer customer = new SecondaryCustomer();
                customer.setName("Concurrent-" + uniqueId);
                secondaryCustomerRepository.save(customer);
            }
        }

        private void verifyTenantDataIsolation(String tenantName, int threadIndex) {
            if (isPrimaryTenant(tenantName)) {
                List<PrimaryCustomer> customers = primaryCustomerRepository.findAll();
                // Verify we can read data (basic connectivity test)
                assertThat(customers).isNotNull();
            } else {
                List<SecondaryCustomer> customers = secondaryCustomerRepository.findAll();
                // Verify we can read data (basic connectivity test)
                assertThat(customers).isNotNull();
            }
        }

        private void createAndVerifyTenantData(String tenantName, int iteration) {
            String dataId = tenantName + "-rapid-" + iteration;
            createTenantSpecificData(tenantName, dataId);
            verifyDataInTenant(tenantName, dataId);
        }

        private void verifyDataInTenant(String tenantName, String dataId) {
            if (isPrimaryTenant(tenantName)) {
                List<PrimaryCustomer> customers = primaryCustomerRepository.findAll();
                assertThat(customers).anySatisfy(c -> assertThat(c.getText()).contains(dataId));
            } else {
                List<SecondaryCustomer> customers = secondaryCustomerRepository.findAll();
                boolean found = customers.stream()
                        .anyMatch(c -> c.getName() != null && c.getName().contains(dataId));
                assertThat(found)
                        .as("Data with ID " + dataId + " should exist in secondary tenant " + tenantName)
                        .isTrue();
            }
        }

        private void verifyAllTenantsHaveData() {
            // Only verify tenants that have proper database setup
            DatabaseType[] validTenants = getValidTenants();
            for (DatabaseType tenant : validTenants) {
                String tenantName = tenant.getSchemaName();
                tenantIdentifierResolver.setCurrentTenant(tenantName);

                if (isPrimaryTenant(tenantName)) {
                    List<PrimaryCustomer> customers = primaryCustomerRepository.findAll();
                    assertThat(customers)
                            .as("Primary tenant " + tenantName + " should have data")
                            .isNotEmpty();
                } else {
                    List<SecondaryCustomer> customers = secondaryCustomerRepository.findAll();
                    assertThat(customers)
                            .as("Secondary tenant " + tenantName + " should have data")
                            .isNotEmpty();
                }
            }
        }

        private boolean isPrimaryTenant(String tenantName) {
            return DatabaseType.PRIMARY.getSchemaName().equals(tenantName)
                    || DatabaseType.DBSYSTC.getSchemaName().equals(tenantName)
                    || DatabaseType.DBSYSTP.getSchemaName().equals(tenantName)
                    || DatabaseType.DBSYSTV.getSchemaName().equals(tenantName);
        }
    }
}
