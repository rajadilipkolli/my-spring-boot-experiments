package com.example.multitenancy.partition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.multitenancy.partition.common.AbstractIntegrationTest;
import com.example.multitenancy.partition.config.TenantIdentifierResolver;
import com.example.multitenancy.partition.entities.Customer;
import com.example.multitenancy.partition.repositories.CustomerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.transaction.support.TransactionTemplate;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    private static final String SUBSYSTEM = "dbsystv";
    private static final String SUBSYSTEM_P = "dbsystp";
    @Autowired TransactionTemplate txTemplate;

    @Autowired TenantIdentifierResolver currentTenant;

    @Autowired CustomerRepository customerRepository;

    @AfterEach
    void afterEach() {
        currentTenant.setCurrentTenant(SUBSYSTEM);
        customerRepository.deleteAll();
        currentTenant.setCurrentTenant(SUBSYSTEM_P);
        customerRepository.deleteAll();
    }

    @Test
    void saveAndLoadCustomer() {

        final Customer rock = createCustomer(SUBSYSTEM_P, "Rock");
        final Customer stoneCold = createCustomer(SUBSYSTEM, "Stonecold");

        assertThat(rock.getTenant()).isEqualTo(SUBSYSTEM_P);
        assertThat(stoneCold.getTenant()).isEqualTo(SUBSYSTEM);

        currentTenant.setCurrentTenant(SUBSYSTEM);
        assertThat(customerRepository.findAll())
                .extracting(Customer::getText)
                .containsExactly("Stonecold");

        currentTenant.setCurrentTenant(SUBSYSTEM_P);
        assertThat(customerRepository.findAll())
                .extracting(Customer::getText)
                .containsExactly("Rock");
    }

    @Test
    void findById() {

        final Customer rock = createCustomer(SUBSYSTEM_P, "Rock");
        final Customer vRock = createCustomer(SUBSYSTEM, "Rock");

        currentTenant.setCurrentTenant(SUBSYSTEM);
        assertThat(customerRepository.findById(vRock.getId())).isPresent();
        assertThat(customerRepository.findById(vRock.getId()).get().getTenant())
                .isEqualTo(SUBSYSTEM);
        assertThat(customerRepository.findById(rock.getId())).isEmpty();
    }

    @Test
    void queryJPQL() {

        createCustomer(SUBSYSTEM_P, "Rock");
        createCustomer(SUBSYSTEM, "Rock");
        createCustomer(SUBSYSTEM, "Stonecold");

        currentTenant.setCurrentTenant(SUBSYSTEM);
        assertThat(customerRepository.findJpqlByText("Rock").getTenant()).isEqualTo(SUBSYSTEM);

        currentTenant.setCurrentTenant(SUBSYSTEM_P);
        assertThat(customerRepository.findJpqlByText("Stonecold")).isNull();
    }

    @Test
    void querySQL() {

        createCustomer(SUBSYSTEM_P, "Rock");
        createCustomer(SUBSYSTEM, "Rock");

        currentTenant.setCurrentTenant(SUBSYSTEM);
        assertThatThrownBy(() -> customerRepository.findSqlByText("Rock"))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    private Customer createCustomer(String schema, String name) {

        currentTenant.setCurrentTenant(schema);

        var customerObj =
                txTemplate.execute(
                        tx -> {
                            Customer customer = new Customer(null, name);
                            return customerRepository.save(customer);
                        });

        assertThat(customerObj.getId()).isNotNull();
        return customerObj;
    }
}
