package com.example.custom.sequence.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.custom.sequence.common.AbstractIntegrationTest;
import com.example.custom.sequence.entities.Customer;
import com.example.custom.sequence.model.request.CustomerRequest;
import com.example.custom.sequence.model.request.OrderRequest;
import com.example.custom.sequence.model.response.CustomerResponse;
import com.example.custom.sequence.model.response.PagedResult;
import io.hypersistence.utils.jdbc.validator.SQLStatementCountValidator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

class CustomerControllerIT extends AbstractIntegrationTest {

    private List<Customer> customerList = null;

    /** Replaces customer fixtures and resets SQL counts before each integration test. */
    @BeforeEach
    void setUp() {
        customerRepository.deleteAllInBatch();

        customerList = new ArrayList<>();
        customerList.add(new Customer().setName("First Customer"));
        customerList.add(new Customer().setName("Second Customer"));
        customerList.add(new Customer().setName("Third Customer"));
        customerList = customerRepository.persistAll(customerList);

        SQLStatementCountValidator.reset();
    }

    @Test
    void shouldFetchAllCustomers() {

        this.mockMvcTester
                .get()
                .uri("/api/customers")
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(PagedResult.class)
                .satisfies(pagedResult -> {
                    assertThat(pagedResult.data()).hasSize(3);
                    assertThat(pagedResult.totalElements()).isEqualTo(3);
                    assertThat(pagedResult.pageNumber()).isOne();
                    assertThat(pagedResult.totalPages()).isOne();
                    assertThat(pagedResult.isFirst()).isTrue();
                    assertThat(pagedResult.isLast()).isTrue();
                    assertThat(pagedResult.hasNext()).isFalse();
                    assertThat(pagedResult.hasPrevious()).isFalse();
                });

        SQLStatementCountValidator.assertSelectCount(2);
        SQLStatementCountValidator.assertTotalCount(2);
    }

    /** Verifies that fetching a customer returns its name and orders with one select. */
    @Test
    void shouldFindCustomerById() {
        Customer customer = customerList.getFirst();
        String customerId = customer.getId();

        this.mockMvcTester
                .get()
                .uri("/api/customers/{id}", customerId)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(CustomerResponse.class)
                .satisfies(customerResponse -> {
                    assertThat(customerResponse.id()).isEqualTo(customer.getId());
                    assertThat(customerResponse.customerName()).isEqualTo(customer.getName());
                    assertThat(customerResponse.orderResponses()).isEmpty();
                });

        SQLStatementCountValidator.assertSelectCount(1);
        SQLStatementCountValidator.assertTotalCount(1);
    }

    /** Verifies that customer creation uses a pooled identifier and only one insert. */
    @Test
    void shouldCreateNewCustomer() {
        CustomerRequest customerRequest = new CustomerRequest("New Customer", null);

        this.mockMvcTester
                .post()
                .uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(customerRequest))
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(CustomerResponse.class)
                .satisfies(customerResponse -> {
                    assertThat(customerResponse.id()).startsWith("CUS").hasSize(8);
                    assertThat(customerResponse.customerName()).isEqualTo(customerRequest.customerName());
                    assertThat(customerResponse.orderResponses()).isEmpty();
                });

        SQLStatementCountValidator.assertSelectCount(0); // should use pooled sequence value
        SQLStatementCountValidator.assertInsertCount(1);
        SQLStatementCountValidator.assertTotalCount(1);

        assertThat(customerRepository.count()).isEqualTo(4);
    }

    /** Verifies that a missing customer name produces a validation error without executing SQL. */
    @Test
    void shouldReturn400WhenCreateNewCustomerWithoutCustomerName() {
        CustomerRequest customerRequest = new CustomerRequest(null, List.of(new OrderRequest("First Order", null)));

        this.mockMvcTester
                .post()
                .uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(customerRequest))
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(errorResponse -> {
                    assertThat(errorResponse.getType())
                            .hasToString("https://custom-sequence.com/errors/validation-error");
                    assertThat(errorResponse.getTitle()).isEqualTo("Constraint Violation");
                    assertThat(errorResponse.getStatus()).isEqualTo(400);
                    assertThat(errorResponse.getDetail()).isEqualTo("Invalid request content.");
                    assertThat(Objects.requireNonNull(errorResponse.getInstance()))
                            .hasToString("/api/customers");
                    assertThat(errorResponse.getProperties()).hasSize(1);
                    Object violations = errorResponse.getProperties().get("violations");
                    assertThat(violations).isInstanceOf(List.class);
                    assertThat((List<?>) violations).hasSize(1);
                    assertThat(((List<?>) violations)).first().isInstanceOf(LinkedHashMap.class);
                    @SuppressWarnings("unchecked")
                    LinkedHashMap<String, Object> violation =
                            (LinkedHashMap<String, Object>) ((List<?>) violations).getFirst();
                    assertThat(violation).containsEntry("field", "customerName");
                    assertThat(violation).containsEntry("message", "Customer name cannot be blank");
                });

        SQLStatementCountValidator.assertTotalCount(0);

        assertThat(customerRepository.count()).isEqualTo(3);
    }

    /** Verifies customer renaming and order reconciliation, including SQL counts for each update. */
    @Test
    void shouldUpdateCustomer() {
        Customer customer = customerList.getFirst();
        CustomerRequest customerRequest = new CustomerRequest("Updated Customer", null);

        this.mockMvcTester
                .put()
                .uri("/api/customers/{id}", customer.getId())
                .content(jsonMapper.writeValueAsString(customerRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(CustomerResponse.class)
                .satisfies(customerResponse -> {
                    assertThat(customerResponse.customerName()).isEqualTo("Updated Customer");
                    assertThat(customerResponse.orderResponses()).isEmpty();
                });

        // select for customer
        SQLStatementCountValidator.assertSelectCount(1);
        // update for customer table
        SQLStatementCountValidator.assertUpdateCount(1);
        SQLStatementCountValidator.assertInsertCount(0);
        SQLStatementCountValidator.assertDeleteCount(0);
        SQLStatementCountValidator.assertTotalCount(2);

        List<OrderRequest> orders = new ArrayList<>();
        orders.add(new OrderRequest("First Order", customer.getId()));
        orders.add(new OrderRequest("Second Order", customer.getId()));

        customerRequest = new CustomerRequest("Updated Customer1", orders);

        SQLStatementCountValidator.reset();

        this.mockMvcTester
                .put()
                .uri("/api/customers/{id}", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(customerRequest))
                .assertThat()
                .hasContentType(MediaType.APPLICATION_JSON)
                .hasStatusOk()
                .bodyJson()
                .convertTo(CustomerResponse.class)
                .satisfies(customerResponse -> {
                    assertThat(customerResponse.customerName()).isEqualTo("Updated Customer1");
                    assertThat(customerResponse.orderResponses()).isNotEmpty().hasSize(2);
                });
        // select for customer and 1 for orders sequence
        SQLStatementCountValidator.assertSelectCount(2);
        // update for customer table
        SQLStatementCountValidator.assertUpdateCount(1);
        // bulk insert for orders
        SQLStatementCountValidator.assertInsertCount(1);
        SQLStatementCountValidator.assertDeleteCount(0);
        SQLStatementCountValidator.assertTotalCount(4);

        orders = new ArrayList<>();
        orders.add(new OrderRequest("Third Order", customer.getId()));
        orders.add(new OrderRequest("Second Order", customer.getId()));

        customerRequest = new CustomerRequest("Updated Customer1", orders);

        SQLStatementCountValidator.reset();

        this.mockMvcTester
                .put()
                .uri("/api/customers/{id}", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(customerRequest))
                .assertThat()
                .hasContentType(MediaType.APPLICATION_JSON)
                .hasStatusOk()
                .bodyJson()
                .convertTo(CustomerResponse.class)
                .satisfies(customerResponse -> {
                    assertThat(customerResponse.customerName()).isEqualTo("Updated Customer1");
                    assertThat(customerResponse.orderResponses()).isNotEmpty().hasSize(2);
                });
        // select for customer
        SQLStatementCountValidator.assertSelectCount(1);
        // update for customer table
        SQLStatementCountValidator.assertUpdateCount(0);
        // bulk insert for orders
        SQLStatementCountValidator.assertInsertCount(1);
        // delete for first order
        SQLStatementCountValidator.assertDeleteCount(1);
        SQLStatementCountValidator.assertTotalCount(3);
    }

    /** Verifies that deletion returns the customer name and removes the row with the expected SQL counts. */
    @Test
    void shouldDeleteCustomer() {
        Customer customer = customerList.getFirst();

        this.mockMvcTester
                .delete()
                .uri("/api/customers/{id}", customer.getId())
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(CustomerResponse.class)
                .satisfies(customerResponse ->
                        assertThat(customerResponse.customerName()).isEqualTo(customer.getName()));

        // select for customer only
        SQLStatementCountValidator.assertSelectCount(1);
        SQLStatementCountValidator.assertUpdateCount(0);
        SQLStatementCountValidator.assertTotalCount(2);
        // delete for customer
        SQLStatementCountValidator.assertDeleteCount(1);

        assertThat(customerRepository.findById(customer.getId())).isEmpty();
    }
}
