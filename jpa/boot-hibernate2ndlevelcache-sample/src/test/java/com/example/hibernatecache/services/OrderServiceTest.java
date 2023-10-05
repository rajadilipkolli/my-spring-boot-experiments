package com.example.hibernatecache.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import com.example.hibernatecache.entities.Order;
import com.example.hibernatecache.mapper.Mapper;
import com.example.hibernatecache.model.request.OrderRequest;
import com.example.hibernatecache.model.response.OrderResponse;
import com.example.hibernatecache.model.response.PagedResult;
import com.example.hibernatecache.repositories.OrderRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private Mapper mapper;

    @InjectMocks private OrderService orderService;

    @Test
    void findAllOrders() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Order> orderPage = new PageImpl<>(List.of(getOrder()));
        given(orderRepository.findAll(pageable)).willReturn(orderPage);
        given(mapper.mapToOrderResponseList(List.of(getOrder())))
                .willReturn(List.of(getOrderResponse()));

        // when
        PagedResult<OrderResponse> pagedResult = orderService.findAllOrders(0, 10, "id", "asc");

        // then
        assertThat(pagedResult).isNotNull();
        assertThat(pagedResult.data()).isNotEmpty().hasSize(1);
        assertThat(pagedResult.hasNext()).isFalse();
        assertThat(pagedResult.pageNumber()).isEqualTo(1);
        assertThat(pagedResult.totalPages()).isEqualTo(1);
        assertThat(pagedResult.isFirst()).isTrue();
        assertThat(pagedResult.isLast()).isTrue();
        assertThat(pagedResult.hasPrevious()).isFalse();
        assertThat(pagedResult.totalElements()).isEqualTo(1);
    }

    @Test
    void findOrderById() {
        // given
        given(orderRepository.findById(1L)).willReturn(Optional.of(getOrder()));
        given(mapper.orderToOrderResponse(getOrder())).willReturn(getOrderResponse());
        // when
        Optional<OrderResponse> optionalOrder = orderService.findOrderById(1L);
        // then
        assertThat(optionalOrder).isPresent();
        OrderResponse order = optionalOrder.get();
        assertThat(order.orderId()).isEqualTo(1L);
        assertThat(order.name()).isEqualTo("junitTest");
    }

    @Test
    void saveOrder() {
        // given
        given(mapper.mapToOrder(getOrderRequest())).willReturn(getOrder());
        given(orderRepository.save(getOrder())).willReturn(getOrder());
        given(mapper.orderToOrderResponse(getOrder())).willReturn(getOrderResponse());

        // when
        OrderResponse persistedOrder = orderService.saveOrderRequest(getOrderRequest());
        // then
        assertThat(persistedOrder).isNotNull();
        assertThat(persistedOrder.customerId()).isEqualTo(1L);
        assertThat(persistedOrder.orderId()).isEqualTo(1L);
        assertThat(persistedOrder.name()).isEqualTo("junitTest");
    }

    private OrderRequest getOrderRequest() {
        return new OrderRequest(1L, "junitTest", BigDecimal.TEN);
    }

    @Test
    void deleteOrderById() {
        // given
        willDoNothing().given(orderRepository).deleteById(1L);
        // when
        orderService.deleteOrderById(1L);
        // then
        verify(orderRepository, times(1)).deleteById(1L);
    }

    private Order getOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setName("junitTest");
        return order;
    }

    private OrderResponse getOrderResponse() {
        return new OrderResponse(1L, 1L, "junitTest", BigDecimal.TEN, new ArrayList<>());
    }
}
