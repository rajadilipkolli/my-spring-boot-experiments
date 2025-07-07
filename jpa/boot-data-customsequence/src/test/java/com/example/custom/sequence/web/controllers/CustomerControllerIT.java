package com.example.custom.sequence.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.custom.sequence.common.AbstractIntegrationTest;
import com.example.custom.sequence.entities.Customer;
import com.example.custom.sequence.model.request.CustomerRequest;
import com.example.custom.sequence.model.request.OrderRequest;
import com.example.custom.sequence.model.response.CustomerResponse;
import com.example.custom.sequence.model.response.PagedResult;
import com.example.custom.sequence.repositories.CustomerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.hypersistence.utils.jdbc.validator.SQLStatementCountValidator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

class CustomerControllerIT extends AbstractIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    private List<Customer> customerList = null;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAllInBatch();

        customerList = new ArrayList<>();
        customerList.add(new Customer().setText("First Customer"));
        customerList.add(new Customer().setText("Second Customer"));
        customerList.add(new Customer().setText("Third Customer"));
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
                    assertThat(pagedResult.pageNumber()).isEqualTo(1);
                    assertThat(pagedResult.totalPages()).isEqualTo(1);
                    assertThat(pagedResult.isFirst()).isTrue();
                    assertThat(pagedResult.isLast()).isTrue();
                    assertThat(pagedResult.hasNext()).isFalse();
                    assertThat(pagedResult.hasPrevious()).isFalse();
                });

        SQLStatementCountValidator.assertSelectCount(2);
        SQLStatementCountValidator.assertTotalCount(2);
    }

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
                    assertThat(customerResponse.text()).isEqualTo(customer.getText());
                    assertThat(customerResponse.orderResponses()).isEmpty();
                });

        SQLStatementCountValidator.assertSelectCount(1);
        SQLStatementCountValidator.assertTotalCount(1);
    }

    @Test
    void shouldCreateNewCustomer() throws Exception {
        CustomerRequest customerRequest = new CustomerRequest("New Customer", null);

        this.mockMvcTester
                .post()
                .uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerRequest))
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(CustomerResponse.class)
                .satisfies(customerResponse -> {
                    assertThat(customerResponse.id()).startsWith("CUS").hasSize(8);
                    assertThat(customerResponse.text()).isEqualTo(customerRequest.text());
                    assertThat(customerResponse.orderResponses()).isEmpty();
                });

        SQLStatementCountValidator.assertSelectCount(0);
        SQLStatementCountValidator.assertInsertCount(1);
        SQLStatementCountValidator.assertTotalCount(1);

        assertThat(customerRepository.count()).isEqualTo(4);
    }

    @Test
    void shouldReturn400WhenCreateNewCustomerWithoutText() throws JsonProcessingException {
        CustomerRequest customerRequest = new CustomerRequest(null, List.of(new OrderRequest("First Order", null)));

        this.mockMvcTester
                .post()
                .uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerRequest))
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(errorResponse -> {
                    assertThat(errorResponse.getType().toString()).isEqualTo("about:blank");
                    assertThat(errorResponse.getTitle()).isEqualTo("Constraint Violation");
                    assertThat(errorResponse.getStatus()).isEqualTo(400);
                    assertThat(errorResponse.getDetail()).isEqualTo("Invalid request content.");
                    assertThat(Objects.requireNonNull(errorResponse.getInstance())
                                    .toString())
                            .isEqualTo("/api/customers");
                    assertThat(errorResponse.getProperties()).hasSize(1);
                    Object violations = errorResponse.getProperties().get("violations");
                    assertThat(violations).isNotNull();
                    assertThat(violations).isInstanceOf(List.class);
                    assertThat((List<?>) violations).hasSize(1);
                    assertThat(((List<?>) violations).getFirst()).isInstanceOf(LinkedHashMap.class);
                    LinkedHashMap<?, ?> violation = (LinkedHashMap<?, ?>) ((List<?>) violations).getFirst();
                    assertThat(violation.get("field")).isEqualTo("text");
                    assertThat(violation.get("message")).isEqualTo("Text cannot be empty");
                });

        SQLStatementCountValidator.assertTotalCount(0);

        assertThat(customerRepository.count()).isEqualTo(3);
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        Customer customer = customerList.getFirst();
        CustomerRequest customerRequest = new CustomerRequest("Updated Customer", null);

        this.mockMvcTester
                .put()
                .uri("/api/customers/{id}", customer.getId())
                .content(objectMapper.writeValueAsString(customerRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(CustomerResponse.class)
                .satisfies(customerResponse -> {
                    assertThat(customerResponse.text()).isEqualTo("Updated Customer");
                    assertThat(customerResponse.orderResponses()).isEmpty();
                });

        // select for customer
        SQLStatementCountValidator.assertSelectCount(1);
        // update for customer table
        SQLStatementCountValidator.assertUpdateCount(1);
        SQLStatementCountValidator.assertInsertCount(0);
        SQLStatementCountValidator.assertDeleteCount(0);

        List<OrderRequest> orders = new ArrayList<>();
        orders.add(new OrderRequest("First Order", customer.getId()));
        orders.add(new OrderRequest("Second Order", customer.getId()));

        customerRequest = new CustomerRequest("Updated Customer1", orders);

        SQLStatementCountValidator.reset();

        this.mockMvcTester
                .put()
                .uri("/api/customers/{id}", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerRequest))
                .assertThat()
                .hasContentType(MediaType.APPLICATION_JSON)
                .hasStatusOk()
                .bodyJson()
                .convertTo(CustomerResponse.class)
                .satisfies(customerResponse -> {
                    assertThat(customerResponse.text()).isEqualTo("Updated Customer1");
                    assertThat(customerResponse.orderResponses()).isNotEmpty().hasSize(2);
                });
        // select for customer and 2 for orders sequence
        SQLStatementCountValidator.assertSelectCount(3);
        // update for customer table
        SQLStatementCountValidator.assertUpdateCount(1);
        // bulk insert for orders
        SQLStatementCountValidator.assertInsertCount(1);
        SQLStatementCountValidator.assertDeleteCount(0);

        orders = new ArrayList<>();
        orders.add(new OrderRequest("Third Order", customer.getId()));
        orders.add(new OrderRequest("Second Order", customer.getId()));

        customerRequest = new CustomerRequest("Updated Customer1", orders);

        SQLStatementCountValidator.reset();

        this.mockMvcTester
                .put()
                .uri("/api/customers/{id}", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerRequest))
                .assertThat()
                .hasContentType(MediaType.APPLICATION_JSON)
                .hasStatusOk()
                .bodyJson()
                .convertTo(CustomerResponse.class)
                .satisfies(customerResponse -> {
                    assertThat(customerResponse.text()).isEqualTo("Updated Customer1");
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
    }

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
                .satisfies(
                        customerResponse -> assertThat(customerResponse.text()).isEqualTo(customer.getText()));

        // select for customer
        SQLStatementCountValidator.assertSelectCount(1);
        SQLStatementCountValidator.assertUpdateCount(0);
        SQLStatementCountValidator.assertInsertCount(0);
        // delete for customer
        SQLStatementCountValidator.assertDeleteCount(1);

        assertThat(customerRepository.findById(customer.getId())).isEmpty();
    }
}
