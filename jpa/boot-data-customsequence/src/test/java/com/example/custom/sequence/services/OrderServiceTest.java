package com.example.custom.sequence.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import com.example.custom.sequence.entities.Customer;
import com.example.custom.sequence.entities.Order;
import com.example.custom.sequence.model.response.OrderDTO;
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
        PagedResult<OrderDTO> pagedResult = orderService.findAllOrders(0, 10, "id", "asc");

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
        given(orderRepository.findById("1")).willReturn(Optional.of(getOrder()));
        // when
        Optional<OrderDTO> optionalOrder = orderService.findOrderById("1");
        // then
        assertThat(optionalOrder).isPresent();
        OrderDTO order = optionalOrder.get();
        assertThat(order.id()).isEqualTo("1");
        assertThat(order.text()).isEqualTo("junitText");
    }

    @Test
    void saveOrder() {
        // given
        given(orderRepository.save(getOrder())).willReturn(getOrder());
        // when
        OrderDTO persistedOrder = orderService.saveOrder(getOrder());
        // then
        assertThat(persistedOrder).isNotNull();
        assertThat(persistedOrder.id()).isEqualTo("1");
        assertThat(persistedOrder.text()).isEqualTo("junitText");
    }

    @Test
    void deleteOrderById() {
        // given
        willDoNothing().given(orderRepository).deleteById("1");
        // when
        orderService.deleteOrderById("1");
        // then
        verify(orderRepository, times(1)).deleteById("1");
    }

    private Order getOrder() {
        Order order = new Order();
        order.setId("1");
        order.setText("junitText");
        Customer customer = new Customer();
        customer.setId("1");
        customer.setText("custText");
        order.setCustomer(customer);
        return order;
    }
}
