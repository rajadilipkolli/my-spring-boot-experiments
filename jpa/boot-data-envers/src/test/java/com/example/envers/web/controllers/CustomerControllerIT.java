package com.example.envers.web.controllers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.example.envers.common.AbstractIntegrationTest;
import com.example.envers.entities.Customer;
import com.example.envers.model.request.CustomerRequest;
import com.example.envers.repositories.CustomerRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class CustomerControllerIT extends AbstractIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    private List<Customer> customerList = null;

    @BeforeEach
    void setUp() {
        RestAssured.port = localServerPort;
        customerRepository.deleteAllInBatch();

        customerList = new ArrayList<>();
        customerList.add(new Customer().setName("First Customer").setAddress("Junit Address1"));
        customerList.add(new Customer().setName("Second Customer").setAddress("Junit Address2"));
        customerList.add(new Customer().setName("Third Customer").setAddress("Junit Address3"));
        customerList = customerRepository.saveAll(customerList);
    }

    @Test
    void shouldFetchAllCustomers() {
        given().when()
                .get("/api/customers")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("data.size()", equalTo(customerList.size()))
                .body("totalElements", equalTo(3))
                .body("pageNumber", equalTo(1))
                .body("totalPages", equalTo(1))
                .body("isFirst", equalTo(true))
                .body("isLast", equalTo(true))
                .body("hasNext", equalTo(false))
                .body("hasPrevious", equalTo(false));
    }

    @Nested
    @DisplayName("find methods")
    class Find {

        @Test
        void shouldFindCustomerById() {
            Customer customer = customerList.getFirst();
            Long customerId = customer.getId();

            given().pathParam("id", customerId)
                    .when()
                    .get("/api/customers/{id}")
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .contentType(ContentType.JSON)
                    .body("id", equalTo(customer.getId().intValue()))
                    .body("name", equalTo(customer.getName()))
                    .body("address", equalTo(customer.getAddress()));
        }

        @Test
        void shouldFindCustomerRevisionsById() {
            Customer customer = customerList.getFirst();
            Long customerId = customer.getId();

            given().pathParam("id", customerId)
                    .when()
                    .get("/api/customers/{id}/revisions")
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .contentType(ContentType.JSON)
                    .body("size()", equalTo(1))
                    .body("[0].entity.id", equalTo(customer.getId().intValue()))
                    .body("[0].entity.name", equalTo(customer.getName()))
                    .body("[0].entity.address", equalTo(customer.getAddress()))
                    .body("[0].revisionNumber", notNullValue())
                    .body("[0].revisionType", equalTo("INSERT"));
        }

        @Test
        void shouldFindCustomerHistoryById() {
            Customer customer = customerList.getFirst();
            customerRepository.saveAndFlush(customer.setAddress("newAddress"));
            Long customerId = customer.getId();

            given().pathParam("id", customerId)
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .queryParam("sort", "revision_Number,desc")
                    .when()
                    .get("/api/customers/{id}/history")
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .contentType(ContentType.JSON)
                    .body("data.size()", equalTo(2))
                    .body("totalElements", equalTo(2))
                    .body("pageNumber", equalTo(1))
                    .body("totalPages", equalTo(1))
                    .body("isFirst", equalTo(true))
                    .body("isLast", equalTo(true))
                    .body("hasNext", equalTo(false))
                    .body("hasPrevious", equalTo(false))
                    .body("data[0].entity.id", equalTo(customer.getId().intValue()))
                    .body("data[0].entity.name", equalTo(customer.getName()))
                    .body("data[0].entity.address", equalTo(customer.getAddress()))
                    .body("data[0].revisionNumber", notNullValue())
                    .body("data[0].revisionType", equalTo("UPDATE"))
                    .body("data[0].revisionInstant", notNullValue());
        }

        @Test
        void cantFindCustomerHistoryById() {
            Customer customer = customerList.getFirst();
            Long customerId = customer.getId() + 10_000;

            given().pathParam("id", customerId)
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .queryParam("sort", "revision_Number,desc")
                    .when()
                    .get("/api/customers/{id}/history")
                    .then()
                    .statusCode(HttpStatus.SC_NOT_FOUND)
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                    .body("type", equalTo("https://api.boot-data-envers.com/errors/not-found"))
                    .body("title", equalTo("Not Found"))
                    .body("status", equalTo(404))
                    .body("detail", equalTo("Customer with Id '%d' not found".formatted(customerId)));
        }
    }

    @Test
    void shouldCreateNewCustomer() {
        CustomerRequest customerRequest = new CustomerRequest("New Customer", "Junit Address");
        given().contentType(ContentType.JSON)
                .body(customerRequest)
                .when()
                .post("/api/customers")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("name", equalTo(customerRequest.name()))
                .body("address", equalTo(customerRequest.address()));
    }

    @Test
    void shouldReturn400WhenCreateNewCustomerWithoutName() {
        CustomerRequest customerRequest = new CustomerRequest(null, null);

        given().contentType(ContentType.JSON)
                .body(customerRequest)
                .when()
                .post("/api/customers")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                .body("type", equalTo("https://api.boot-data-envers.com/errors/validation"))
                .body("title", equalTo("Constraint Violation"))
                .body("status", equalTo(400))
                .body("detail", equalTo("Invalid request content."))
                .body("instance", equalTo("/api/customers"))
                .body("properties.violations", hasSize(1))
                .body("properties.violations[0].field", equalTo("name"))
                .body("properties.violations[0].message", equalTo("Name cannot be empty"));
    }

    @Test
    void shouldUpdateCustomer() {
        Long customerId = customerList.getFirst().getId();
        CustomerRequest customerRequest = new CustomerRequest("Updated Customer", "Junit Address");

        given().pathParam("id", customerId)
                .contentType(ContentType.JSON)
                .body(customerRequest)
                .when()
                .put("/api/customers/{id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON)
                .body("id", equalTo(customerId.intValue()))
                .body("name", equalTo(customerRequest.name()))
                .body("address", equalTo("Junit Address"));
    }

    @Test
    void shouldDeleteCustomer() {
        Customer customer = customerList.getFirst();

        given().pathParam("id", customer.getId())
                .when()
                .delete("/api/customers/{id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON)
                .body("id", equalTo(customer.getId().intValue()))
                .body("name", equalTo(customer.getName()))
                .body("address", equalTo(customer.getAddress()));
    }
}
