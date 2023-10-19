package com.example.custom.sequence.services;

import com.example.custom.sequence.entities.Order;
import com.example.custom.sequence.mapper.OrderMapper;
import com.example.custom.sequence.model.response.OrderResponse;
import com.example.custom.sequence.model.response.PagedResult;
import com.example.custom.sequence.repositories.OrderRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public PagedResult<OrderResponse> findAllOrders(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Order> ordersPage = orderRepository.findAll(pageable);

        List<OrderResponse> orderResponseList =
                ordersPage.getContent().stream().map(orderMapper::getOrderResponse).toList();

        return new PagedResult<>(
                orderResponseList,
                ordersPage.getTotalElements(),
                ordersPage.getNumber() + 1,
                ordersPage.getTotalPages(),
                ordersPage.isFirst(),
                ordersPage.isLast(),
                ordersPage.hasNext(),
                ordersPage.hasPrevious());
    }

    public Optional<OrderResponse> findOrderById(String id) {
        return orderRepository.findById(id).map(orderMapper::getOrderResponse);
    }

    public OrderResponse saveOrder(Order order) {
        return orderMapper.getOrderResponse(orderRepository.save(order));
    }

    public void deleteOrderById(String id) {
        orderRepository.deleteById(id);
    }
}
