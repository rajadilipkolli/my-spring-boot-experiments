package com.example.envers.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.envers.common.AbstractIntegrationTest;
import com.example.envers.entities.Customer;
import com.example.envers.model.request.CustomerRequest;
import com.example.envers.model.response.CustomerResponse;
import com.example.envers.model.response.PagedResult;
import com.example.envers.model.response.RevisionResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

class CustomerControllerIT extends AbstractIntegrationTest {

    private List<Customer> customerList = null;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAllInBatch();

        customerList = new ArrayList<>();
        customerList.add(new Customer().setName("First Customer").setAddress("Junit Address1"));
        customerList.add(new Customer().setName("Second Customer").setAddress("Junit Address2"));
        customerList.add(new Customer().setName("Third Customer").setAddress("Junit Address3"));
        customerList = customerRepository.saveAll(customerList);
    }

    @Test
    void shouldFetchAllCustomers() {
        mockMvcTester
                .get()
                .uri("/api/customers")
                .exchange()
                .assertThat()
                .hasContentType(MediaType.APPLICATION_JSON)
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(PagedResult.class)
                .satisfies(pagedResult -> {
                    assertThat(pagedResult).isNotNull();
                    assertThat(pagedResult.data()).isNotEmpty().hasSameSizeAs(customerList);
                    assertThat(pagedResult.totalElements()).isEqualTo(3);
                    assertThat(pagedResult.pageNumber()).isOne();
                    assertThat(pagedResult.totalPages()).isOne();
                    assertThat(pagedResult.isFirst()).isTrue();
                    assertThat(pagedResult.isLast()).isTrue();
                    assertThat(pagedResult.hasNext()).isFalse();
                    assertThat(pagedResult.hasPrevious()).isFalse();
                });
    }

    @Nested
    @DisplayName("find methods")
    class Find {

        @Test
        void shouldFindCustomerById() {
            Customer customer = customerList.getFirst();
            Long customerId = customer.getId();

            mockMvcTester
                    .get()
                    .uri("/api/customers/{id}", customer.getId())
                    .exchange()
                    .assertThat()
                    .hasStatus(HttpStatus.OK)
                    .hasContentType(MediaType.APPLICATION_JSON)
                    .bodyJson()
                    .convertTo(CustomerResponse.class)
                    .satisfies(customerResponse -> {
                        assertThat(customerResponse).isNotNull();
                        assertThat(customerResponse.id()).isEqualTo(customerId);
                        assertThat(customerResponse.name()).isEqualTo(customer.getName());
                        assertThat(customerResponse.address()).isEqualTo(customer.getAddress());
                    });
        }

        @Test
        void shouldFindCustomerRevisionsById() {
            Customer customer = customerList.getFirst();
            Long customerId = customer.getId();

            mockMvcTester
                    .get()
                    .uri("/api/customers/{id}/revisions", customerId)
                    .exchange()
                    .assertThat()
                    .hasStatus(HttpStatus.OK)
                    .hasContentType(MediaType.APPLICATION_JSON)
                    .bodyJson()
                    .convertTo(RevisionResult[].class)
                    .satisfies(revisionResults -> {
                        assertThat(revisionResults).hasSize(1);
                        assertThat(revisionResults[0].entity().getId()).isEqualTo(customerId);
                        assertThat(revisionResults[0].entity().getName()).isEqualTo(customer.getName());
                        assertThat(revisionResults[0].entity().getAddress()).isEqualTo(customer.getAddress());
                        assertThat(revisionResults[0].revisionNumber()).isNotNull();
                        assertThat(revisionResults[0].revisionType()).isEqualTo("INSERT");
                    });
        }

        @Test
        void shouldFindCustomerHistoryById() {
            Customer customer = customerList.getFirst();
            customerRepository.saveAndFlush(customer.setAddress("newAddress"));
            Long customerId = customer.getId();

            mockMvcTester
                    .get()
                    .uri("/api/customers/{id}/history", customerId)
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "revision_Number,desc")
                    .exchange()
                    .assertThat()
                    .hasStatus(HttpStatus.OK)
                    .hasContentType(MediaType.APPLICATION_JSON)
                    .bodyJson()
                    .convertTo(PagedResult.class)
                    .satisfies(pagedResult -> {
                        assertThat(pagedResult).isNotNull();
                        assertThat(pagedResult.data()).isNotEmpty().hasSize(2);
                        assertThat(pagedResult.totalElements()).isEqualTo(2);
                        assertThat(pagedResult.pageNumber()).isOne();
                        assertThat(pagedResult.totalPages()).isOne();
                        assertThat(pagedResult.isFirst()).isTrue();
                        assertThat(pagedResult.isLast()).isTrue();
                        assertThat(pagedResult.hasNext()).isFalse();
                        assertThat(pagedResult.hasPrevious()).isFalse();
                        @SuppressWarnings("unchecked")
                        Map<String, Object> firstMap =
                                (Map<String, Object>) pagedResult.data().getFirst();
                        assertThat(firstMap.get("revisionNumber")).isNotNull();
                        assertThat(firstMap).containsEntry("revisionType", "UPDATE");
                        assertThat(firstMap.get("revisionInstant")).isNotNull();
                        @SuppressWarnings("unchecked")
                        Map<String, Object> entityMap = (Map<String, Object>) firstMap.get("entity");
                        // entity.id is an integer in the JSON
                        Number idNum = (Number) entityMap.get("id");
                        assertThat(idNum.longValue()).isEqualTo(customerId.longValue());
                        assertThat(entityMap)
                                .containsEntry("name", customer.getName())
                                .containsEntry("address", customer.getAddress());
                    });
        }

        @Test
        void cantFindCustomerHistoryById() {
            Customer customer = customerList.getFirst();
            Long customerId = customer.getId() + 10_000;

            mockMvcTester
                    .get()
                    .uri("/api/customers/{id}/history", customerId)
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "revision_Number,desc")
                    .exchange()
                    .assertThat()
                    .hasStatus(HttpStatus.NOT_FOUND)
                    .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .bodyJson()
                    .convertTo(ProblemDetail.class)
                    .satisfies(problemDetail -> {
                        assertThat(problemDetail).isNotNull();
                        assertThat(problemDetail.getType()).isNotNull();
                        assertThat(problemDetail.getType())
                                .hasToString("https://api.boot-data-envers.com/errors/not-found");
                        assertThat(problemDetail.getTitle()).isEqualTo("Not Found");
                        assertThat(problemDetail.getStatus()).isEqualTo(404);
                        assertThat(problemDetail.getDetail())
                                .isEqualTo("Customer with Id '%d' not found".formatted(customerId));
                    });
        }
    }

    @Test
    void shouldCreateNewCustomer() {
        CustomerRequest customerRequest = new CustomerRequest("New Customer", "Junit Address");

        mockMvcTester
                .post()
                .uri("/api/customers")
                .content(jsonMapper.writeValueAsString(customerRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasContentType(MediaType.APPLICATION_JSON)
                .containsHeader(HttpHeaders.LOCATION)
                .bodyJson()
                .convertTo(CustomerResponse.class)
                .satisfies(customerResponse -> {
                    assertThat(customerResponse).isNotNull();
                    assertThat(customerResponse.id()).isNotNull();
                    assertThat(customerResponse.name()).isEqualTo(customerRequest.name());
                    assertThat(customerResponse.address()).isEqualTo(customerRequest.address());
                });
    }

    @Test
    void shouldReturn400WhenCreateNewCustomerWithoutName() {
        CustomerRequest customerRequest = new CustomerRequest(null, null);

        mockMvcTester
                .post()
                .uri("/api/customers")
                .content(jsonMapper.writeValueAsString(customerRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(problem -> {
                    assertThat(problem.getType()).isNotNull();
                    assertThat(problem.getType()).hasToString("https://api.boot-data-envers.com/errors/validation");
                    assertThat(problem.getTitle()).isEqualTo("Constraint Violation");
                    assertThat(problem.getStatus()).isEqualTo(400);
                    assertThat(problem.getDetail()).isEqualTo("Invalid request content.");
                    assertThat(problem.getInstance()).isNotNull();
                    assertThat(problem.getInstance()).hasToString("/api/customers");

                    @SuppressWarnings("unchecked")
                    var violations = (java.util.List<Map<String, Object>>)
                            problem.getProperties().get("violations");
                    assertThat(violations).hasSize(1);
                    assertThat(violations.getFirst())
                            .containsEntry("field", "name")
                            .containsEntry("message", "Name cannot be empty");
                });
    }

    @Test
    void shouldUpdateCustomer() {
        Long customerId = customerList.getFirst().getId();
        CustomerRequest customerRequest = new CustomerRequest("Updated Customer", "Junit Address");

        mockMvcTester
                .put()
                .uri("/api/customers/{id}", customerId)
                .content(jsonMapper.writeValueAsString(customerRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(CustomerResponse.class)
                .satisfies(customer -> {
                    assertThat(customer.name()).isEqualTo("Updated Customer");
                    assertThat(customer.address()).isEqualTo("Junit Address");
                    assertThat(customer.id()).isEqualTo(customerId);
                });
    }

    @Test
    void shouldDeleteCustomer() {
        Customer customer = customerList.getFirst();

        mockMvcTester
                .delete()
                .uri("/api/customers/{id}", customer.getId())
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(CustomerResponse.class)
                .satisfies(customerResponse -> {
                    assertThat(customerResponse).isNotNull();
                    assertThat(customerResponse.id()).isEqualTo(customer.getId());
                    assertThat(customerResponse.name()).isEqualTo(customer.getName());
                    assertThat(customerResponse.address()).isEqualTo(customer.getAddress());
                });
    }
}
