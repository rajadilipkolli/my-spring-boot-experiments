package com.example.custom.sequence.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import com.example.custom.sequence.entities.Order;
import com.example.custom.sequence.model.response.PagedResult;
import com.example.custom.sequence.repositories.OrderRepository;
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

    @InjectMocks private OrderService orderService;

    @Test
    void findAllOrders() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Order> orderPage = new PageImpl<>(List.of(getOrder()));
        given(orderRepository.findAll(pageable)).willReturn(orderPage);

        // when
        PagedResult<Order> pagedResult = orderService.findAllOrders(0, 10, "id", "asc");

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
        // when
        Optional<Order> optionalOrder = orderService.findOrderById(1L);
        // then
        assertThat(optionalOrder).isPresent();
        Order order = optionalOrder.get();
        assertThat(order.getId()).isEqualTo(1L);
        assertThat(order.getText()).isEqualTo("junitTest");
    }

    @Test
    void saveOrder() {
        // given
        given(orderRepository.save(getOrder())).willReturn(getOrder());
        // when
        Order persistedOrder = orderService.saveOrder(getOrder());
        // then
        assertThat(persistedOrder).isNotNull();
        assertThat(persistedOrder.getId()).isEqualTo(1L);
        assertThat(persistedOrder.getText()).isEqualTo("junitTest");
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
        order.setText("junitTest");
        return order;
    }
}
