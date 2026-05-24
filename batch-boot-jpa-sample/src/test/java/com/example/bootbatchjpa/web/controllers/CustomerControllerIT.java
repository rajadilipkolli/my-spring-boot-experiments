package com.example.bootbatchjpa.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

import com.example.bootbatchjpa.common.AbstractIntegrationTest;
import com.example.bootbatchjpa.entities.Customer;
import com.example.bootbatchjpa.model.response.PagedResult;
import com.example.bootbatchjpa.repositories.CustomerRepository;
import java.net.URI;
import java.util.List;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

class CustomerControllerIT extends AbstractIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    private List<Customer> customerList = null;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAllInBatch();

        customerList = Instancio.ofList(Customer.class)
                .size(3)
                .ignore(field(Customer.class, "id"))
                .generate(field(Customer.class, "gender"), gen -> gen.oneOf("male", "female"))
                .create();

        customerList = customerRepository.saveAll(customerList);
    }

    @Test
    void shouldFetchAllCustomers() {
        this.mockMvcTester
                .get()
                .uri("/api/customers")
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON_VALUE)
                .bodyJson()
                .convertTo(PagedResult.class)
                .satisfies(pagedResult -> {
                    assertThat(pagedResult.data()).hasSize(customerList.size());
                    assertThat(pagedResult.totalElements()).isEqualTo(3);
                    assertThat(pagedResult.pageNumber()).isEqualTo(1);
                    assertThat(pagedResult.totalPages()).isEqualTo(1);
                    assertThat(pagedResult.isFirst()).isEqualTo(true);
                    assertThat(pagedResult.isLast()).isEqualTo(true);
                    assertThat(pagedResult.hasNext()).isEqualTo(false);
                    assertThat(pagedResult.hasPrevious()).isEqualTo(false);
                });
    }

    @Test
    void shouldFindCustomerById() {
        Customer customer = customerList.getFirst();
        Long customerId = customer.getId();

        this.mockMvcTester
                .get()
                .uri("/api/customers/{id}", customerId)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON_VALUE)
                .bodyJson()
                .convertTo(Customer.class)
                .satisfies(fetchedCustomer -> {
                    assertThat(fetchedCustomer.getId()).isEqualTo(customer.getId());
                    assertThat(fetchedCustomer.getName()).isEqualTo(customer.getName());
                });
    }

    @Test
    void shouldCreateNewCustomer() {
        Customer customer = Instancio.create(Customer.class);
        customer.setId(null);
        this.mockMvcTester
                .post()
                .uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonMapper.writeValueAsString(customer))
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(201)
                .hasContentType(MediaType.APPLICATION_JSON_VALUE)
                .bodyJson()
                .convertTo(Customer.class)
                .satisfies(createdCustomer -> {
                    assertThat(createdCustomer.getId()).isNotNull();
                    assertThat(createdCustomer.getName()).isEqualTo(customer.getName());
                });
    }

    @Test
    void shouldReturn400WhenCreateNewCustomerWithoutName() {
        Customer customer = new Customer(null, null, null, null);

        this.mockMvcTester
                .post()
                .uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonMapper.writeValueAsString(customer))
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(400)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(problemDetail -> {
                    assertThat(problemDetail.getType()).isEqualTo(URI.create("https://boot-jpa.com/errors/validation"));
                    assertThat(problemDetail.getTitle()).isEqualTo("Constraint Violation");
                    assertThat(problemDetail.getStatus()).isEqualTo(400);
                    assertThat(problemDetail.getDetail()).isEqualTo("Invalid request content.");
                    assertThat(problemDetail.getInstance()).isEqualTo(URI.create("/api/customers"));
                    assertThat(problemDetail.getProperties())
                            .containsKey("violations")
                            .extracting("violations")
                            .asInstanceOf(InstanceOfAssertFactories.LIST)
                            .hasSize(1)
                            .first()
                            .asInstanceOf(InstanceOfAssertFactories.MAP)
                            .containsEntry("field", "name")
                            .containsEntry("message", "Name cannot be empty")
                            .containsEntry("object", "customer")
                            .containsEntry("rejectedValue", null);
                });
    }

    @Test
    void shouldUpdateCustomer() {
        Customer customer = customerList.getFirst();
        customer.setName("Updated Customer");

        this.mockMvcTester
                .put()
                .uri("/api/customers/{id}", customer.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonMapper.writeValueAsString(customer))
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(200)
                .hasContentType(MediaType.APPLICATION_JSON_VALUE)
                .bodyJson()
                .convertTo(Customer.class)
                .satisfies(updatedCustomer -> {
                    assertThat(updatedCustomer.getId()).isEqualTo(customer.getId());
                    assertThat(updatedCustomer.getName()).isEqualTo("Updated Customer");
                });
    }

    @Test
    void shouldDeleteCustomer() {
        Customer customer = customerList.getFirst();

        this.mockMvcTester
                .delete()
                .uri("/api/customers/{id}", customer.getId())
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON_VALUE)
                .bodyJson()
                .convertTo(Customer.class)
                .satisfies(deletedCustomer -> {
                    assertThat(deletedCustomer.getId()).isEqualTo(customer.getId());
                    assertThat(deletedCustomer.getName()).isEqualTo(customer.getName());
                });
    }
}
