package com.example.hibernatecache.services;

import com.example.hibernatecache.entities.Order;
import com.example.hibernatecache.mapper.Mapper;
import com.example.hibernatecache.model.request.OrderRequest;
import com.example.hibernatecache.model.response.OrderResponse;
import com.example.hibernatecache.model.response.PagedResult;
import com.example.hibernatecache.repositories.OrderRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final Mapper mapper;

    @Autowired
    public OrderService(OrderRepository orderRepository, Mapper mapper) {
        this.orderRepository = orderRepository;
        this.mapper = mapper;
    }

    public PagedResult<OrderResponse> findAllOrders(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Order> ordersPage = orderRepository.findAll(pageable);

        List<OrderResponse> orderResponses = mapper.mapToOrderResponseList(ordersPage.getContent());
        Page<OrderResponse> orderResponsePage =
                new PageImpl<>(orderResponses, pageable, ordersPage.getTotalElements());
        return new PagedResult<>(orderResponsePage);
    }

    public Optional<OrderResponse> findOrderById(Long id) {
        return findById(id).map(mapper::orderToOrderResponse);
    }

    public OrderResponse saveOrderRequest(OrderRequest orderRequest) {

        Order order = mapper.mapToOrder(orderRequest);
        return saveOrder(order);
    }

    private OrderResponse saveOrder(Order order) {
        Order saved = orderRepository.save(order);
        return mapper.orderToOrderResponse(saved);
    }

    public void deleteOrderById(Long id) {
        orderRepository.deleteById(id);
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public OrderResponse updateOrder(Order orderObj, OrderRequest orderRequest) {
        mapper.updateOrderWithRequest(orderRequest, orderObj);
        return saveOrder(orderObj);
    }
}
