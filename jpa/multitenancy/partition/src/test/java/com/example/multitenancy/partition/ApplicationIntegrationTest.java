package com.example.multitenancy.partition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.multitenancy.partition.common.AbstractIntegrationTest;
import com.example.multitenancy.partition.config.tenant.TenantIdentifierResolver;
import com.example.multitenancy.partition.entities.Customer;
import com.example.multitenancy.partition.repositories.CustomerRepository;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(ApplicationIntegrationTest.class);

    private static final String SUBSYSTEM_V = "dbsystv";
    private static final String SUBSYSTEM_P = "dbsystp";
    private static final String SUBSYSTEM_C = "dbsystc";

    @Autowired TransactionTemplate txTemplate;

    @Autowired TenantIdentifierResolver tenantIdentifierResolver;

    @Autowired CustomerRepository customerRepository;

    @AfterEach
    void afterEach() {
        tenantIdentifierResolver.setCurrentTenant(SUBSYSTEM_V);
        customerRepository.deleteAllInBatch();
        tenantIdentifierResolver.setCurrentTenant(SUBSYSTEM_P);
        customerRepository.deleteAllInBatch();
        tenantIdentifierResolver.setCurrentTenant(SUBSYSTEM_C);
        customerRepository.deleteAllInBatch();
    }

    @Test
    void sequenceCollision() throws Exception {
        long count = this.customerRepository.count();
        for (int i = 0; i < 153; i++) {
            Customer customer = new Customer(null, RandomStringUtils.randomAlphanumeric(10), null);
            String tenant;
            if (i % 3 == 0) {
                tenant = SUBSYSTEM_P;
            } else if ((i % 3 == 1)) {
                tenant = SUBSYSTEM_V;
            } else {
                tenant = SUBSYSTEM_C;
            }
            String response =
                    this.mockMvc
                            .perform(
                                    post("/api/customers")
                                            .param("tenant", tenant)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(customer)))
                            .andExpect(status().isCreated())
                            .andExpect(jsonPath("$.text", is(customer.getText())))
                            .andExpect(jsonPath("$.id", notNullValue()))
                            .andExpect(jsonPath("$.tenant", is(tenant)))
                            .andReturn()
                            .getResponse()
                            .getContentAsString(StandardCharsets.UTF_8);
            log.info("Response :{}", response);
        }
        assertThat(this.customerRepository.countByTenant(SUBSYSTEM_V)).isEqualTo(count);
        assertThat(this.customerRepository.countByTenant(SUBSYSTEM_P)).isEqualTo(count);
        assertThat(this.customerRepository.countByTenant(SUBSYSTEM_C)).isEqualTo(count + 51);
        // querying db also needs tenant to be set hence setting
        tenantIdentifierResolver.setCurrentTenant(SUBSYSTEM_V);
        assertThat(this.customerRepository.countByTenant(SUBSYSTEM_V)).isEqualTo(count + 51);
        tenantIdentifierResolver.setCurrentTenant(SUBSYSTEM_P);
        assertThat(this.customerRepository.countByTenant(SUBSYSTEM_P)).isEqualTo(count + 51);
        tenantIdentifierResolver.setCurrentTenant(SUBSYSTEM_C);
        assertThat(this.customerRepository.countByTenant(SUBSYSTEM_C)).isEqualTo(count + 51);
    }

    @Test
    void saveAndLoadCustomer() {

        final Customer rock = createCustomer(SUBSYSTEM_P, "Rock");
        final Customer stoneCold = createCustomer(SUBSYSTEM_V, "Stonecold");

        assertThat(rock.getTenant()).isEqualTo(SUBSYSTEM_P);
        assertThat(stoneCold.getTenant()).isEqualTo(SUBSYSTEM_V);

        tenantIdentifierResolver.setCurrentTenant(SUBSYSTEM_V);
        assertThat(customerRepository.findAll())
                .extracting(Customer::getText)
                .containsExactly("Stonecold");

        tenantIdentifierResolver.setCurrentTenant(SUBSYSTEM_P);
        assertThat(customerRepository.findAll())
                .extracting(Customer::getText)
                .containsExactly("Rock");
    }

    @Test
    void findById() {

        final Customer rock = createCustomer(SUBSYSTEM_P, "Rock");
        final Customer vRock = createCustomer(SUBSYSTEM_V, "Rock");

        tenantIdentifierResolver.setCurrentTenant(SUBSYSTEM_V);
        assertThat(customerRepository.findById(vRock.getId())).isPresent().isNotEmpty();
        assertThat(customerRepository.findById(vRock.getId()).get().getTenant())
                .isEqualTo(SUBSYSTEM_V);
        assertThat(customerRepository.findById(rock.getId())).isEmpty();
    }

    @Test
    void queryJPQL() {

        createCustomer(SUBSYSTEM_P, "Rock");
        createCustomer(SUBSYSTEM_V, "Rock");
        createCustomer(SUBSYSTEM_V, "Stonecold");

        tenantIdentifierResolver.setCurrentTenant(SUBSYSTEM_V);
        assertThat(customerRepository.findJpqlByText("Rock").getTenant()).isEqualTo(SUBSYSTEM_V);

        tenantIdentifierResolver.setCurrentTenant(SUBSYSTEM_P);
        assertThat(customerRepository.findJpqlByText("Stonecold")).isNull();
    }

    @Test
    void querySQL() {

        createCustomer(SUBSYSTEM_P, "Rock");
        createCustomer(SUBSYSTEM_V, "Rock");

        tenantIdentifierResolver.setCurrentTenant(SUBSYSTEM_V);
        assertThatThrownBy(() -> customerRepository.findSqlByText("Rock"))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    private Customer createCustomer(String schema, String name) {

        tenantIdentifierResolver.setCurrentTenant(schema);

        var customerObj =
                txTemplate.execute(
                        tx -> {
                            Customer customer = new Customer(null, name);
                            return customerRepository.save(customer);
                        });

        assertThat(customerObj.getId()).isNotNull();
        assertThat(customerObj.getTenant()).isEqualTo(schema);
        return customerObj;
    }
}
