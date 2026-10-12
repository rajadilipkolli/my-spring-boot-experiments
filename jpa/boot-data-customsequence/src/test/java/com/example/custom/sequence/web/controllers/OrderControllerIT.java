package com.example.custom.sequence.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.custom.sequence.common.AbstractIntegrationTest;
import com.example.custom.sequence.entities.Customer;
import com.example.custom.sequence.entities.Order;
import com.example.custom.sequence.model.request.OrderRequest;
import com.example.custom.sequence.model.response.OrderResponse;
import com.example.custom.sequence.model.response.PagedResult;
import io.hypersistence.utils.jdbc.validator.SQLStatementCountValidator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

class OrderControllerIT extends AbstractIntegrationTest {

    private List<Order> orderList = null;
    private Customer customer;

    /** Replaces order and customer fixtures and resets SQL counts before each integration test. */
    @BeforeEach
    void setUp() {
        orderRepository.deleteAllInBatch();
        customerRepository.deleteAllInBatch();
        customer = createTestCustomer();
        orderList = createTestOrders(customer);

        SQLStatementCountValidator.reset();
    }

    /** Persists the named customer used as the owner of test orders. */
    private Customer createTestCustomer() {
        return customerRepository.persist(new Customer().setName("customer1"));
    }

    /**
     * Persists three named order fixtures for the supplied customer.
     *
     * @param customer the owner of the test orders
     * @return the persisted orders with generated identifiers
     */
    private List<Order> createTestOrders(Customer customer) {
        List<Order> orders = List.of(
                new Order().setItemName("First Order").setCustomer(customer),
                new Order().setItemName("Second Order").setCustomer(customer),
                new Order().setItemName("Third Order").setCustomer(customer));
        return orderRepository.persistAll(orders);
    }

    /** Verifies order pagination metadata and the two selects used to fetch the page. */
    @Test
    void shouldFetchAllOrders() {

        this.mockMvcTester
                .get()
                .uri("/api/orders")
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

    /** Verifies that fetching an order returns its persisted identifier and item name. */
    @Test
    void shouldFindOrderById() {
        Order order = orderList.getFirst();
        String orderId = order.getId();

        this.mockMvcTester
                .get()
                .uri("/api/orders/{id}", orderId)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(OrderResponse.class)
                .satisfies(orderResponse -> {
                    assertThat(orderResponse.id()).isEqualTo(orderId);
                    assertThat(orderResponse.itemName()).isEqualTo(order.getItemName());
                });
    }

    /** Verifies that order creation returns a nine-character identifier using one select and one insert. */
    @Test
    void shouldCreateNewOrder() {
        OrderRequest orderRequest = new OrderRequest("New Order", customer.getId());

        this.mockMvcTester
                .post()
                .uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(orderRequest))
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(OrderResponse.class)
                .satisfies(orderResponse -> {
                    assertThat(orderResponse.id()).hasSize(9);
                    assertThat(orderResponse.itemName()).isEqualTo(orderRequest.itemName());
                });
        SQLStatementCountValidator.assertSelectCount(1);
        SQLStatementCountValidator.assertInsertCount(1);
        SQLStatementCountValidator.assertTotalCount(2);
    }

    /** Verifies that a missing item name produces a validation error without executing SQL. */
    @Test
    void shouldReturn400WhenCreateNewOrderWithoutItemName() {
        OrderRequest orderRequest = new OrderRequest(null, "CUS_1");

        this.mockMvcTester
                .post()
                .uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(orderRequest))
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(problem -> {
                    assertThat(problem.getType()).hasToString("https://custom-sequence.com/errors/validation-error");
                    assertThat(problem.getTitle()).isEqualTo("Constraint Violation");
                    assertThat(problem.getStatus()).isEqualTo(400);
                    assertThat(problem.getDetail()).isEqualTo("Invalid request content.");
                    assertThat(Objects.requireNonNull(problem.getInstance())).hasToString("/api/orders");
                    assertThat(problem.getProperties()).hasSize(1);
                    Object violations = problem.getProperties().get("violations");
                    assertThat(violations).isInstanceOf(List.class);
                    assertThat((List<?>) violations).hasSize(1);
                    assertThat(((List<?>) violations)).first().isInstanceOf(LinkedHashMap.class);
                    @SuppressWarnings("unchecked")
                    LinkedHashMap<String, Object> violation =
                            (LinkedHashMap<String, Object>) ((List<?>) violations).getFirst();
                    assertThat(violation).containsEntry("field", "itemName");
                    assertThat(violation).containsEntry("message", "Item name cannot be blank");
                });
        SQLStatementCountValidator.assertSelectCount(0);
        SQLStatementCountValidator.assertInsertCount(0);
        SQLStatementCountValidator.assertTotalCount(0);
    }

    /** Verifies that updating an item name preserves the order identifier and uses one select and one update. */
    @Test
    void shouldUpdateOrder() {
        OrderRequest orderRequest = new OrderRequest("Updated Order", customer.getId());
        Order order = orderList.getFirst();

        this.mockMvcTester
                .put()
                .uri("/api/orders/{id}", order.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(orderRequest))
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(OrderResponse.class)
                .satisfies(orderResponse -> {
                    assertThat(orderResponse.id()).isEqualTo(order.getId());
                    assertThat(orderResponse.itemName()).isEqualTo(orderRequest.itemName());
                });

        // select for customer
        SQLStatementCountValidator.assertSelectCount(1);
        // update for order table
        SQLStatementCountValidator.assertUpdateCount(1);
        SQLStatementCountValidator.assertInsertCount(0);
        SQLStatementCountValidator.assertDeleteCount(0);
    }

    /** Verifies that an update with an unmatched customer identifier returns HTTP 404. */
    @Test
    void shouldReturn400WhenUpdatingOrderWithInvalidCustomerId() {
        OrderRequest orderRequest = new OrderRequest("Updated Order", "INVALID_ID");
        Order order = orderList.getFirst();

        this.mockMvcTester
                .put()
                .uri("/api/orders/{id}", order.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(orderRequest))
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    /** Verifies that updating a nonexistent order returns HTTP 404. */
    @Test
    void shouldReturn404WhenUpdatingNonExistentOrder() {
        OrderRequest orderRequest = new OrderRequest("Updated Order", customer.getId());

        this.mockMvcTester
                .put()
                .uri("/api/orders/{id}", "NON_EXISTENT_ID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(orderRequest))
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    /** Verifies that deletion returns the order details and removes the row with two selects and one delete. */
    @Test
    void shouldDeleteOrder() {
        Order order = orderList.getFirst();
        var orderId = order.getId();

        this.mockMvcTester
                .delete()
                .uri("/api/orders/{id}", orderId)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(OrderResponse.class)
                .satisfies(orderResponse -> {
                    assertThat(orderResponse.id()).isEqualTo(orderId);
                    assertThat(orderResponse.itemName()).isEqualTo(order.getItemName());
                });

        // select for order and customer
        SQLStatementCountValidator.assertSelectCount(2);
        SQLStatementCountValidator.assertUpdateCount(0);
        SQLStatementCountValidator.assertInsertCount(0);
        // delete for order table
        SQLStatementCountValidator.assertDeleteCount(1);
        SQLStatementCountValidator.assertTotalCount(3);
        // Verify order is deleted from database
        assertThat(orderRepository.findById(orderId)).isEmpty();
    }
}
