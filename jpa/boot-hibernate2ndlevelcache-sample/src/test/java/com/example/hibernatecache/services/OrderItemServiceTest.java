package com.example.hibernatecache.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import com.example.hibernatecache.entities.OrderItem;
import com.example.hibernatecache.mapper.OrderItemMapper;
import com.example.hibernatecache.model.response.OrderItemResponse;
import com.example.hibernatecache.repositories.OrderItemRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderItemService orderItemService;

    @Test
    void findOrderItemById() {
        // given
        given(orderItemRepository.findById(1L)).willReturn(Optional.of(getOrderItem()));
        given(orderItemMapper.toResponse(any(OrderItem.class))).willReturn(getOrderItemResponse());
        // when
        Optional<OrderItemResponse> optionalOrderItem = orderItemService.findOrderItemById(1L);
        // then
        assertThat(optionalOrderItem).isPresent();
        OrderItemResponse orderItem = optionalOrderItem.get();
        assertThat(orderItem.id()).isEqualTo(1L);
        assertThat(orderItem.text()).isEqualTo("junitTest");
    }

    @Test
    void deleteOrderItemById() {
        // given
        willDoNothing().given(orderItemRepository).deleteById(1L);
        // when
        orderItemService.deleteOrderItemById(1L);
        // then
        verify(orderItemRepository, times(1)).deleteById(1L);
    }

    private OrderItem getOrderItem() {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setText("junitTest");
        return orderItem;
    }

    private OrderItemResponse getOrderItemResponse() {
        return new OrderItemResponse(1L, "junitTest");
    }
}
