package com.example.custom.sequence.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.custom.sequence.common.AbstractIntegrationTest;
import com.example.custom.sequence.entities.Customer;
import com.example.custom.sequence.entities.Order;
import com.example.custom.sequence.model.request.OrderRequest;
import com.example.custom.sequence.model.response.OrderResponse;
import com.example.custom.sequence.model.response.PagedResult;
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

    @BeforeEach
    void setUp() {
        orderRepository.deleteAllInBatch();
        customerRepository.deleteAllInBatch();
        customer = createTestCustomer();
        orderList = createTestOrders(customer);
    }

    private Customer createTestCustomer() {
        return customerRepository.persist(new Customer().setText("customer1"));
    }

    private List<Order> createTestOrders(Customer customer) {
        List<Order> orders = List.of(
                new Order().setText("First Order").setCustomer(customer),
                new Order().setText("Second Order").setCustomer(customer),
                new Order().setText("Third Order").setCustomer(customer));
        return orderRepository.persistAll(orders);
    }

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
    }

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
                    assertThat(orderResponse.text()).isEqualTo(order.getText());
                });
    }

    @Test
    void shouldCreateNewOrder() throws Exception {
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
                    assertThat(orderResponse.text()).isEqualTo(orderRequest.text());
                });
    }

    @Test
    void shouldReturn400WhenCreateNewOrderWithoutText() throws Exception {
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
                    LinkedHashMap<?, ?> violation = (LinkedHashMap<?, ?>) ((List<?>) violations).getFirst();
                    assertThat(violation).containsEntry("field", "text");
                    assertThat(violation).containsEntry("message", "Text cannot be empty");
                });
    }

    @Test
    void shouldUpdateOrder() throws Exception {
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
                    assertThat(orderResponse.text()).isEqualTo(orderRequest.text());
                });
    }

    @Test
    void shouldReturn400WhenUpdatingOrderWithInvalidCustomerId() throws Exception {
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

    @Test
    void shouldReturn404WhenUpdatingNonExistentOrder() throws Exception {
        OrderRequest orderRequest = new OrderRequest("Updated Order", customer.getId());

        this.mockMvcTester
                .put()
                .uri("/api/orders/{id}", "NON_EXISTENT_ID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(orderRequest))
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND);
    }

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
                    assertThat(orderResponse.text()).isEqualTo(order.getText());
                });

        // Verify order is deleted from database
        assertThat(orderRepository.findById(orderId)).isEmpty();
    }
}
