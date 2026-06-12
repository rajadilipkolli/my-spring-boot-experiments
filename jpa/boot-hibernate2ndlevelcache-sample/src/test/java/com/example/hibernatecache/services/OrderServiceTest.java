package com.example.hibernatecache.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.example.hibernatecache.entities.Customer;
import com.example.hibernatecache.entities.Order;
import com.example.hibernatecache.exception.OrderNotFoundException;
import com.example.hibernatecache.mapper.OrderMapper;
import com.example.hibernatecache.model.response.OrderResponse;
import com.example.hibernatecache.repositories.OrderRepository;
import jakarta.persistence.Cache;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private OrderService orderService;

    @Test
    void findOrderById() {
        // given
        given(orderRepository.findById(1L)).willReturn(Optional.of(getOrder()));
        given(orderMapper.toResponse(any(Order.class))).willReturn(getOrderResponse());
        // when
        Optional<OrderResponse> optionalOrder = orderService.findOrderById(1L);
        // then
        assertThat(optionalOrder).isPresent();
        OrderResponse order = optionalOrder.get();
        assertThat(order.orderId()).isOne();
        assertThat(order.name()).isEqualTo("junitTest");
    }

    @Test
    void deleteOrderById() {
        // given
        Order testOrder = getOrder();
        given(orderRepository.findById(1L)).willReturn(Optional.of(testOrder));
        willDoNothing().given(orderRepository).delete(testOrder);

        // Mock EntityManager cache hierarchy
        EntityManagerFactory mockFactory = mock(EntityManagerFactory.class);
        Cache mockCache = mock(Cache.class);
        given(entityManager.getEntityManagerFactory()).willReturn(mockFactory);
        given(mockFactory.getCache()).willReturn(mockCache);
        willDoNothing().given(mockCache).evict(Customer.class, 1L);

        // when
        orderService.deleteOrderById(1L);

        // then
        verify(orderRepository, times(1)).delete(testOrder);
        verify(mockCache, times(1)).evict(Customer.class, 1L);
    }

    @Test
    void deleteOrderById_NotFound() {
        // given
        given(orderRepository.findById(1L)).willReturn(Optional.empty());

        // Mock EntityManager cache hierarchy
        EntityManagerFactory mockFactory = mock(EntityManagerFactory.class);
        Cache mockCache = mock(Cache.class);
        // Mocks kept as requested, but no stubbing needed since they are never called

        // when & then
        assertThrows(OrderNotFoundException.class, () -> orderService.deleteOrderById(1L));

        verify(orderRepository, never()).delete(any());
        verify(mockCache, never()).evict(any(), any());
    }

    private Order getOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setName("junitTest");
        order.setCustomer(new Customer().setId(1L));
        return order;
    }

    private OrderResponse getOrderResponse() {
        return new OrderResponse(1L, 1L, "junitTest", BigDecimal.TEN, new ArrayList<>());
    }
}
