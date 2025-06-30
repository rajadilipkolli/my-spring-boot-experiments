package com.example.multitenancy.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.multitenancy.common.AbstractIntegrationTest;
import com.example.multitenancy.primary.entities.PrimaryCustomer;
import com.example.multitenancy.secondary.entities.SecondaryCustomer;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@DisplayName("Repository Integration Tests")
class RepositoryIntegrationTest extends AbstractIntegrationTest {

    @Nested
    @DisplayName("Primary Customer Repository Tests")
    class PrimaryCustomerRepositoryTests {

        @BeforeEach
        void setUp() {
            tenantIdentifierResolver.setCurrentTenant("primary");
            primaryCustomerRepository.deleteAllInBatch();
        }

        @Test
        @DisplayName("Should save and find primary customer")
        void shouldSaveAndFindPrimaryCustomer() {
            // Given
            PrimaryCustomer customer = new PrimaryCustomer().setText("Primary Test Customer");

            // When
            PrimaryCustomer saved = primaryCustomerRepository.save(customer);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getText()).isEqualTo("Primary Test Customer");
            assertThat(saved.getTenant()).isEqualTo("primary");

            // Verify retrieval
            Optional<PrimaryCustomer> found = primaryCustomerRepository.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getText()).isEqualTo("Primary Test Customer");
        }

        @Test
        @DisplayName("Should find all primary customers")
        void shouldFindAllPrimaryCustomers() {
            // Given
            List<PrimaryCustomer> customers =
                    List.of(
                            new PrimaryCustomer().setText("Customer 1"),
                            new PrimaryCustomer().setText("Customer 2"),
                            new PrimaryCustomer().setText("Customer 3"));
            primaryCustomerRepository.saveAll(customers);

            // When
            List<PrimaryCustomer> allCustomers = primaryCustomerRepository.findAll();

            // Then
            assertThat(allCustomers).hasSize(3);
            assertThat(allCustomers)
                    .extracting(PrimaryCustomer::getText)
                    .containsExactlyInAnyOrder("Customer 1", "Customer 2", "Customer 3");
        }

        @Test
        @DisplayName("Should support pagination")
        void shouldSupportPagination() {
            // Given
            for (int i = 1; i <= 10; i++) {
                primaryCustomerRepository.save(new PrimaryCustomer().setText("Customer " + i));
            }

            // When
            Pageable pageable = PageRequest.of(0, 5);
            Page<PrimaryCustomer> page = primaryCustomerRepository.findAll(pageable);

            // Then
            assertThat(page.getContent()).hasSize(5);
            assertThat(page.getTotalElements()).isEqualTo(10);
            assertThat(page.getTotalPages()).isEqualTo(2);
            assertThat(page.hasNext()).isTrue();
        }

        @Test
        @DisplayName("Should support sorting")
        void shouldSupportSorting() {
            // Given
            primaryCustomerRepository.saveAll(
                    List.of(
                            new PrimaryCustomer().setText("Charlie"),
                            new PrimaryCustomer().setText("Alice"),
                            new PrimaryCustomer().setText("Bob")));

            // When
            List<PrimaryCustomer> sortedCustomers =
                    primaryCustomerRepository.findAll(Sort.by("text"));

            // Then
            assertThat(sortedCustomers)
                    .extracting(PrimaryCustomer::getText)
                    .containsExactly("Alice", "Bob", "Charlie");
        }

        @Test
        @DisplayName("Should delete primary customer")
        void shouldDeletePrimaryCustomer() {
            // Given
            PrimaryCustomer customer =
                    primaryCustomerRepository.save(new PrimaryCustomer().setText("To Delete"));

            // When
            primaryCustomerRepository.deleteById(customer.getId());

            // Then
            Optional<PrimaryCustomer> deleted =
                    primaryCustomerRepository.findById(customer.getId());
            assertThat(deleted).isEmpty();
        }

        @Test
        @DisplayName("Should delete all customers in batch")
        void shouldDeleteAllCustomersInBatch() {
            // Given
            primaryCustomerRepository.saveAll(
                    List.of(
                            new PrimaryCustomer().setText("Customer 1"),
                            new PrimaryCustomer().setText("Customer 2"),
                            new PrimaryCustomer().setText("Customer 3")));

            // When
            primaryCustomerRepository.deleteAllInBatch();

            // Then
            List<PrimaryCustomer> allCustomers = primaryCustomerRepository.findAll();
            assertThat(allCustomers).isEmpty();
        }

        @Test
        @DisplayName("Should count customers correctly")
        void shouldCountCustomersCorrectly() {
            // Given
            primaryCustomerRepository.saveAll(
                    List.of(
                            new PrimaryCustomer().setText("Customer 1"),
                            new PrimaryCustomer().setText("Customer 2")));

            // When
            long count = primaryCustomerRepository.count();

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should check existence by ID")
        void shouldCheckExistenceById() {
            // Given
            PrimaryCustomer customer =
                    primaryCustomerRepository.save(new PrimaryCustomer().setText("Existence Test"));

            // When/Then
            assertThat(primaryCustomerRepository.existsById(customer.getId())).isTrue();
            assertThat(primaryCustomerRepository.existsById(99999L)).isFalse();
        }
    }

    @Nested
    @DisplayName("Secondary Customer Repository Tests")
    class SecondaryCustomerRepositoryTests {

        @BeforeEach
        void setUp() {
            tenantIdentifierResolver.setCurrentTenant("schema1");
            secondaryCustomerRepository.deleteAllInBatch();
        }

        @Test
        @DisplayName("Should save and find secondary customer")
        void shouldSaveAndFindSecondaryCustomer() {
            // Given
            SecondaryCustomer customer = new SecondaryCustomer().setName("Secondary Test Customer");

            // When
            SecondaryCustomer saved = secondaryCustomerRepository.save(customer);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("Secondary Test Customer");

            // Verify retrieval
            Optional<SecondaryCustomer> found = secondaryCustomerRepository.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Secondary Test Customer");
        }

        @Test
        @DisplayName("Should find all secondary customers")
        void shouldFindAllSecondaryCustomers() {
            // Given
            List<SecondaryCustomer> customers =
                    List.of(
                            new SecondaryCustomer().setName("Customer 1"),
                            new SecondaryCustomer().setName("Customer 2"),
                            new SecondaryCustomer().setName("Customer 3"));
            secondaryCustomerRepository.saveAll(customers);

            // When
            List<SecondaryCustomer> allCustomers = secondaryCustomerRepository.findAll();

            // Then
            assertThat(allCustomers).hasSize(3);
            assertThat(allCustomers)
                    .extracting(SecondaryCustomer::getName)
                    .containsExactlyInAnyOrder("Customer 1", "Customer 2", "Customer 3");
        }

        @Test
        @DisplayName("Should support pagination for secondary customers")
        void shouldSupportPaginationForSecondaryCustomers() {
            // Given
            for (int i = 1; i <= 8; i++) {
                secondaryCustomerRepository.save(new SecondaryCustomer().setName("Customer " + i));
            }

            // When
            Pageable pageable = PageRequest.of(1, 3);
            Page<SecondaryCustomer> page = secondaryCustomerRepository.findAll(pageable);

            // Then
            assertThat(page.getContent()).hasSize(3);
            assertThat(page.getTotalElements()).isEqualTo(8);
            assertThat(page.getNumber()).isEqualTo(1);
            assertThat(page.hasNext()).isTrue();
            assertThat(page.hasPrevious()).isTrue();
        }

        @Test
        @DisplayName("Should support sorting for secondary customers")
        void shouldSupportSortingForSecondaryCustomers() {
            // Given
            secondaryCustomerRepository.saveAll(
                    List.of(
                            new SecondaryCustomer().setName("Zebra"),
                            new SecondaryCustomer().setName("Alpha"),
                            new SecondaryCustomer().setName("Beta")));

            // When
            List<SecondaryCustomer> sortedCustomers =
                    secondaryCustomerRepository.findAll(Sort.by("name"));

            // Then
            assertThat(sortedCustomers)
                    .extracting(SecondaryCustomer::getName)
                    .containsExactly("Alpha", "Beta", "Zebra");
        }

        @Test
        @DisplayName("Should delete secondary customer")
        void shouldDeleteSecondaryCustomer() {
            // Given
            SecondaryCustomer customer =
                    secondaryCustomerRepository.save(new SecondaryCustomer().setName("To Delete"));

            // When
            secondaryCustomerRepository.deleteById(customer.getId());

            // Then
            Optional<SecondaryCustomer> deleted =
                    secondaryCustomerRepository.findById(customer.getId());
            assertThat(deleted).isEmpty();
        }

        @Test
        @DisplayName("Should maintain schema isolation")
        void shouldMaintainSchemaIsolation() {
            // Given - Create customer in schema1
            secondaryCustomerRepository.save(new SecondaryCustomer().setName("Schema1 Customer"));

            // When - Switch to schema2 and check
            tenantIdentifierResolver.setCurrentTenant("schema2");
            secondaryCustomerRepository.deleteAllInBatch(); // Clear schema2

            List<SecondaryCustomer> schema2Customers = secondaryCustomerRepository.findAll();

            // Then
            assertThat(schema2Customers).isEmpty();

            // Switch back to schema1 and verify data is still there
            tenantIdentifierResolver.setCurrentTenant("schema1");
            List<SecondaryCustomer> schema1Customers = secondaryCustomerRepository.findAll();
            assertThat(schema1Customers).hasSize(1);
            assertThat(schema1Customers.getFirst().getName()).isEqualTo("Schema1 Customer");
        }

        @Test
        @DisplayName("Should handle concurrent operations across schemas")
        void shouldHandleConcurrentOperationsAcrossSchemas() {
            // Given
            tenantIdentifierResolver.setCurrentTenant("schema1");
            SecondaryCustomer schema1Customer =
                    new SecondaryCustomer().setName("Schema1 Concurrent");

            tenantIdentifierResolver.setCurrentTenant("schema2");
            secondaryCustomerRepository.deleteAllInBatch();
            SecondaryCustomer schema2Customer =
                    new SecondaryCustomer().setName("Schema2 Concurrent");

            // When - Save in both schemas
            tenantIdentifierResolver.setCurrentTenant("schema1");
            SecondaryCustomer saved1 = secondaryCustomerRepository.save(schema1Customer);

            tenantIdentifierResolver.setCurrentTenant("schema2");
            SecondaryCustomer saved2 = secondaryCustomerRepository.save(schema2Customer);

            // Then
            assertThat(saved1.getName()).isEqualTo("Schema1 Concurrent");
            assertThat(saved2.getName()).isEqualTo("Schema2 Concurrent");

            // Verify isolation
            List<SecondaryCustomer> schema2List = secondaryCustomerRepository.findAll();
            assertThat(schema2List).hasSize(1);

            tenantIdentifierResolver.setCurrentTenant("schema1");
            List<SecondaryCustomer> schema1List = secondaryCustomerRepository.findAll();
            assertThat(schema1List).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Cross-Repository Integration Tests")
    class CrossRepositoryIntegrationTests {

        @BeforeEach
        void setUp() {
            tenantIdentifierResolver.setCurrentTenant("primary");
            primaryCustomerRepository.deleteAllInBatch();

            tenantIdentifierResolver.setCurrentTenant("schema1");
            secondaryCustomerRepository.deleteAllInBatch();
        }

        @Test
        @DisplayName("Should maintain complete isolation between primary and secondary datasources")
        void shouldMaintainCompleteIsolationBetweenDatasources() {
            // Given
            tenantIdentifierResolver.setCurrentTenant("primary");
            primaryCustomerRepository.save(new PrimaryCustomer().setText("Primary Customer"));

            tenantIdentifierResolver.setCurrentTenant("schema1");
            secondaryCustomerRepository.save(new SecondaryCustomer().setName("Secondary Customer"));

            // When/Then - Verify each datasource only sees its own data
            tenantIdentifierResolver.setCurrentTenant("primary");
            List<PrimaryCustomer> primaryCustomers = primaryCustomerRepository.findAll();
            assertThat(primaryCustomers).hasSize(1);
            assertThat(primaryCustomers.getFirst().getText()).isEqualTo("Primary Customer");

            tenantIdentifierResolver.setCurrentTenant("schema1");
            List<SecondaryCustomer> secondaryCustomers = secondaryCustomerRepository.findAll();
            assertThat(secondaryCustomers).hasSize(1);
            assertThat(secondaryCustomers.getFirst().getName()).isEqualTo("Secondary Customer");
        }

        @Test
        @DisplayName("Should handle ID generation independently")
        void shouldHandleIdGenerationIndependently() {
            // Given
            tenantIdentifierResolver.setCurrentTenant("primary");
            PrimaryCustomer primary1 =
                    primaryCustomerRepository.save(new PrimaryCustomer().setText("Primary 1"));
            PrimaryCustomer primary2 =
                    primaryCustomerRepository.save(new PrimaryCustomer().setText("Primary 2"));

            tenantIdentifierResolver.setCurrentTenant("schema1");
            SecondaryCustomer secondary1 =
                    secondaryCustomerRepository.save(
                            new SecondaryCustomer().setName("Secondary 1"));
            SecondaryCustomer secondary2 =
                    secondaryCustomerRepository.save(
                            new SecondaryCustomer().setName("Secondary 2"));

            // Then - IDs should be properly generated for each datasource
            assertThat(primary1.getId()).isNotNull();
            assertThat(primary2.getId()).isNotNull();
            assertThat(secondary1.getId()).isNotNull();
            assertThat(secondary2.getId()).isNotNull();

            // IDs should be unique within each datasource
            assertThat(primary1.getId()).isNotEqualTo(primary2.getId());
            assertThat(secondary1.getId()).isNotEqualTo(secondary2.getId());
        }

        @Test
        @DisplayName("Should support bulk operations independently")
        void shouldSupportBulkOperationsIndependently() {
            // Given
            List<PrimaryCustomer> primaryCustomers =
                    List.of(
                            new PrimaryCustomer().setText("Bulk Primary 1"),
                            new PrimaryCustomer().setText("Bulk Primary 2"),
                            new PrimaryCustomer().setText("Bulk Primary 3"));

            List<SecondaryCustomer> secondaryCustomers =
                    List.of(
                            new SecondaryCustomer().setName("Bulk Secondary 1"),
                            new SecondaryCustomer().setName("Bulk Secondary 2"));

            // When
            tenantIdentifierResolver.setCurrentTenant("primary");
            primaryCustomerRepository.saveAll(primaryCustomers);

            tenantIdentifierResolver.setCurrentTenant("schema1");
            secondaryCustomerRepository.saveAll(secondaryCustomers);

            // Then
            tenantIdentifierResolver.setCurrentTenant("primary");
            assertThat(primaryCustomerRepository.count()).isEqualTo(3);

            tenantIdentifierResolver.setCurrentTenant("schema1");
            assertThat(secondaryCustomerRepository.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle batch deletions independently")
        void shouldHandleBatchDeletionsIndependently() {
            // Given - Setup data in both datasources
            tenantIdentifierResolver.setCurrentTenant("primary");
            primaryCustomerRepository.saveAll(
                    List.of(
                            new PrimaryCustomer().setText("Primary 1"),
                            new PrimaryCustomer().setText("Primary 2")));

            tenantIdentifierResolver.setCurrentTenant("schema1");
            secondaryCustomerRepository.saveAll(
                    List.of(
                            new SecondaryCustomer().setName("Secondary 1"),
                            new SecondaryCustomer().setName("Secondary 2"),
                            new SecondaryCustomer().setName("Secondary 3")));

            // When - Delete all from primary datasource only
            tenantIdentifierResolver.setCurrentTenant("primary");
            primaryCustomerRepository.deleteAllInBatch();

            // Then - Primary should be empty, secondary should be unaffected
            assertThat(primaryCustomerRepository.count()).isEqualTo(0);

            tenantIdentifierResolver.setCurrentTenant("schema1");
            assertThat(secondaryCustomerRepository.count()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Performance and Load Tests")
    class PerformanceAndLoadTests {

        @BeforeEach
        void setUp() {
            tenantIdentifierResolver.setCurrentTenant("primary");
            primaryCustomerRepository.deleteAllInBatch();

            tenantIdentifierResolver.setCurrentTenant("schema1");
            secondaryCustomerRepository.deleteAllInBatch();
        }

        @Test
        @DisplayName("Should handle large dataset efficiently")
        void shouldHandleLargeDatasetEfficiently() {
            // Given
            tenantIdentifierResolver.setCurrentTenant("schema1");

            // When - Create a large number of customers
            for (int i = 1; i <= 100; i++) {
                secondaryCustomerRepository.save(new SecondaryCustomer().setName("Customer " + i));
            }

            // Then
            long count = secondaryCustomerRepository.count();
            assertThat(count).isEqualTo(100);

            // Verify pagination works with large datasets
            Pageable pageable = PageRequest.of(0, 20);
            Page<SecondaryCustomer> page = secondaryCustomerRepository.findAll(pageable);
            assertThat(page.getContent()).hasSize(20);
            assertThat(page.getTotalElements()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should handle rapid tenant switching")
        void shouldHandleRapidTenantSwitching() {
            // Given/When - Rapidly switch between tenants and perform operations
            for (int i = 0; i < 10; i++) {
                tenantIdentifierResolver.setCurrentTenant("schema1");
                secondaryCustomerRepository.save(
                        new SecondaryCustomer().setName("Schema1 Customer " + i));

                tenantIdentifierResolver.setCurrentTenant("schema2");
                secondaryCustomerRepository.save(
                        new SecondaryCustomer().setName("Schema2 Customer " + i));
            }

            // Then
            tenantIdentifierResolver.setCurrentTenant("schema1");
            long schema1Count = secondaryCustomerRepository.count();
            assertThat(schema1Count).isEqualTo(10);

            tenantIdentifierResolver.setCurrentTenant("schema2");
            long schema2Count = secondaryCustomerRepository.count();
            assertThat(schema2Count).isEqualTo(10);
        }
    }
}
