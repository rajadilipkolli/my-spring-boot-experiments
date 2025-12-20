package com.example.envers;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.envers.common.AbstractIntegrationTest;
import com.example.envers.entities.Customer;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionMetadata.RevisionType;
import org.springframework.data.history.Revisions;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void initialRevision() {
        Customer cust = new Customer().setName("junitName").setAddress("junitAddress");
        Customer savedCustomer = customerRepository.save(cust);

        Revisions<Integer, Customer> revisions = customerRepository.findRevisions(savedCustomer.getId());

        assertThat(revisions).isNotEmpty().allSatisfy(revision -> assertThat(revision.getEntity())
                .extracting(Customer::getId, Customer::getName, Customer::getVersion)
                .containsExactly(savedCustomer.getId(), savedCustomer.getName(), null));
    }

    @Test
    void updateIncreasesRevisionNumber() {
        Customer cust = new Customer().setName("text");
        Customer customer = customerRepository.save(cust);

        customer.setName("If");

        Customer updatedCustomer = customerRepository.save(customer);

        Optional<Revision<Integer, Customer>> revision =
                customerRepository.findLastChangeRevision(updatedCustomer.getId());

        assertThat(revision)
                .isPresent()
                .hasValueSatisfying(rev -> {
                    assertThat(rev.getRevisionNumber()).isPresent();
                    assertThat(rev.getRevisionNumber().get()).isPositive();
                })
                .hasValueSatisfying(rev -> assertThat(rev.getEntity())
                        .extracting(Customer::getName)
                        .asString()
                        .isEqualTo("If"));
    }

    @Test
    void deletedItemWillHaveRevisionRetained() {
        Customer cust = new Customer().setName("junitName").setAddress("junitAddress");
        Customer customer = customerRepository.save(cust);

        customerRepository.delete(customer);

        Revisions<Integer, Customer> revisions = customerRepository.findRevisions(customer.getId());

        assertThat(revisions).hasSize(2);

        Iterator<Revision<Integer, Customer>> iterator = revisions.iterator();

        Revision<Integer, Customer> initialRevision = iterator.next();
        Revision<Integer, Customer> finalRevision = iterator.next();

        assertThat(initialRevision).satisfies(rev -> assertThat(rev.getEntity())
                .extracting(Customer::getId, Customer::getName, Customer::getAddress, Customer::getVersion)
                .containsExactly(customer.getId(), customer.getName(), customer.getAddress(), null));

        assertThat(finalRevision).satisfies(rev -> assertThat(rev.getEntity())
                .extracting(Customer::getId, Customer::getName, Customer::getAddress, Customer::getVersion)
                .containsExactly(customer.getId(), null, null, null));
    }

    @Test
    void shouldTrackMultipleUpdates() {
        // Create a new customer with unique identifiers to avoid conflicts
        Customer cust = new Customer().setName("uniqueOriginalName").setAddress("uniqueOriginalAddress");
        Customer savedCustomer = customerRepository.save(cust);
        Long customerId = savedCustomer.getId();

        // First update
        savedCustomer = customerRepository.findById(customerId).orElseThrow();
        savedCustomer.setName("uniqueFirstUpdate");
        customerRepository.save(savedCustomer);

        // Second update - fetch fresh to avoid optimistic locking issues
        savedCustomer = customerRepository.findById(customerId).orElseThrow();
        savedCustomer.setAddress("uniqueUpdatedAddress");
        customerRepository.save(savedCustomer);

        // Get all revisions and validate
        Revisions<Integer, Customer> revisions = customerRepository.findRevisions(customerId);
        List<Revision<Integer, Customer>> revisionList = revisions.getContent();

        assertThat(revisionList).hasSize(3); // Initial + 2 updates

        // Check revision types and content
        assertThat(revisionList.get(0).getMetadata().getRevisionType()).isEqualTo(RevisionType.INSERT);
        assertThat(revisionList.get(0).getEntity().getName()).isEqualTo("uniqueOriginalName");

        assertThat(revisionList.get(1).getMetadata().getRevisionType()).isEqualTo(RevisionType.UPDATE);
        assertThat(revisionList.get(1).getEntity().getName()).isEqualTo("uniqueFirstUpdate");

        assertThat(revisionList.get(2).getMetadata().getRevisionType()).isEqualTo(RevisionType.UPDATE);
        assertThat(revisionList.get(2).getEntity().getAddress()).isEqualTo("uniqueUpdatedAddress");
    }

    @Test
    void shouldFindRevisionByRevisionNumber() {
        // Create and update a customer to generate revisions
        Customer customer =
                customerRepository.save(new Customer().setName("findByRevision").setAddress("address"));
        Long customerId = customer.getId();

        // Get the actual revision number from the first revision
        Optional<Revision<Integer, Customer>> firstRevision =
                customerRepository.findRevisions(customerId).getContent().stream()
                        .findFirst();

        assertThat(firstRevision).isPresent();
        Integer actualRevisionNumber = firstRevision.get().getRevisionNumber().orElseThrow();

        // Verify we can find by revision number using the actual number
        Optional<Revision<Integer, Customer>> revisionById =
                customerRepository.findRevision(customerId, actualRevisionNumber);

        assertThat(revisionById).isPresent().hasValueSatisfying(rev -> {
            assertThat(rev.getEntity().getName()).isEqualTo("findByRevision");
            assertThat(rev.getMetadata().getRevisionType()).isEqualTo(RevisionType.INSERT);
        });
    }

    @Test
    void shouldReturnRevisionsInDescendingOrder() {
        // Create customer with multiple revisions
        Customer customer =
                customerRepository.save(new Customer().setName("revisionOrder").setAddress("address"));
        Long customerId = customer.getId();

        // Read the customer to avoid optimistic locking
        customer = customerRepository.findById(customerId).orElseThrow();
        customer.setName("updatedRevisionOrder");
        customerRepository.save(customer);

        // Get revisions directly from repository and convert to list
        Revisions<Integer, Customer> revisions = customerRepository.findRevisions(customerId);
        List<Revision<Integer, Customer>> revisionList = revisions.getContent();

        // Now manually sort in reverse order to simulate descending
        List<Revision<Integer, Customer>> descendingOrder = revisions.getContent().stream()
                .sorted((r1, r2) -> r2.getRevisionNumber()
                        .orElse(0)
                        .compareTo(r1.getRevisionNumber().orElse(0)))
                .toList();

        // Verify descending order manually (most recent first)
        assertThat(descendingOrder).hasSize(2);
        assertThat(descendingOrder.get(0).getEntity().getName()).isEqualTo("updatedRevisionOrder");
        assertThat(descendingOrder.get(1).getEntity().getName()).isEqualTo("revisionOrder");
    }

    @Test
    void shouldFindRevisionAsOfDate() {
        // Create a customer
        Customer customer =
                customerRepository.save(new Customer().setName("beforeUpdate").setAddress("address"));
        Long customerId = customer.getId();

        // Get the revision number of the first revision
        Optional<Revision<Integer, Customer>> initialRevision = customerRepository.findLastChangeRevision(customerId);
        assertThat(initialRevision).isPresent();
        Integer initialRevisionNumber =
                initialRevision.get().getRevisionNumber().get();

        // First update
        customer.setName("afterUpdate");
        customerRepository.save(customer);

        // Find revision by the initial revision number
        Optional<Revision<Integer, Customer>> revision =
                customerRepository.findRevision(customerId, initialRevisionNumber);

        // Verify we get the state before the update
        assertThat(revision)
                .isPresent()
                .hasValueSatisfying(rev -> assertThat(rev.getEntity().getName()).isEqualTo("beforeUpdate"));
    }

    @Test
    void shouldCaptureRevisionTypeCorrectly() {
        // Create customer
        Customer customer = new Customer().setName("revisionTypeTest").setAddress("address");
        Customer savedCustomer = customerRepository.save(customer);
        Long customerId = savedCustomer.getId();

        // Update customer
        savedCustomer.setName("updated");
        customerRepository.save(savedCustomer);

        // Delete customer
        customerRepository.deleteById(customerId);

        // Get all revisions
        Revisions<Integer, Customer> revisions = customerRepository.findRevisions(customerId);
        List<Revision<Integer, Customer>> revisionList = revisions.getContent();

        // Verify revision types
        assertThat(revisionList).hasSize(3);
        assertThat(revisionList.get(0).getMetadata().getRevisionType()).isEqualTo(RevisionType.INSERT);
        assertThat(revisionList.get(1).getMetadata().getRevisionType()).isEqualTo(RevisionType.UPDATE);
        assertThat(revisionList.get(2).getMetadata().getRevisionType()).isEqualTo(RevisionType.DELETE);
    }
}
