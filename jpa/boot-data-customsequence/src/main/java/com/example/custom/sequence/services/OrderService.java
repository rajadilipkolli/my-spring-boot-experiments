package com.example.custom.sequence.services;

import com.example.custom.sequence.entities.Order;
import com.example.custom.sequence.mapper.OrderMapper;
import com.example.custom.sequence.model.request.OrderRequest;
import com.example.custom.sequence.model.response.OrderResponse;
import com.example.custom.sequence.model.response.PagedResult;
import com.example.custom.sequence.repositories.OrderRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository, CustomerService customerService, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.customerService = customerService;
        this.orderMapper = orderMapper;
    }

    public PagedResult<OrderResponse> findAllOrders(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Order> ordersPage = orderRepository.findAll(pageable);

        List<OrderResponse> orderResponseList = ordersPage.getContent().stream()
                .map(orderMapper::getOrderResponse)
                .toList();

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

    @Transactional
    public Optional<OrderResponse> saveOrder(OrderRequest orderRequest) {
        return customerService.findById(orderRequest.customerId()).map(customer -> {
            Order order = new Order();
            order.setText(orderRequest.text());
            order.setCustomer(customer);
            return orderMapper.getOrderResponse(orderRepository.persist(order));
        });
    }

    @Transactional
    public Optional<OrderResponse> updateOrderById(String id, OrderRequest orderRequest) {
        return orderRepository
                .findByIdAndCustomer_Id(id, orderRequest.customerId())
                .map(order -> {
                    order.setText(orderRequest.text());
                    return orderMapper.getOrderResponse(orderRepository.mergeAndFlush(order));
                });
    }

    @Transactional
    public void deleteOrderById(String id) {
        orderRepository.deleteById(id);
    }
}
