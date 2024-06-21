package com.example.hibernatecache.services;

import com.example.hibernatecache.entities.Order;
import com.example.hibernatecache.mapper.Mapper;
import com.example.hibernatecache.model.request.OrderRequest;
import com.example.hibernatecache.model.response.OrderResponse;
import com.example.hibernatecache.model.response.PagedResult;
import com.example.hibernatecache.repositories.OrderRepository;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final Mapper mapper;

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
        return new PagedResult<>(ordersPage, orderResponses);
    }

    public Optional<OrderResponse> findOrderById(Long id) {
        return findById(id).map(mapper::orderToOrderResponse);
    }

    @Transactional
    public OrderResponse saveOrderRequest(OrderRequest orderRequest) {
        Order order = mapper.mapToOrder(orderRequest);
        Order saved = orderRepository.persist(order);
        return mapper.orderToOrderResponse(saved);
    }

    @Transactional
    public void deleteOrderById(Long id) {
        orderRepository.deleteById(id);
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional
    public OrderResponse updateOrder(Order orderObj, OrderRequest orderRequest) {
        mapper.updateOrderWithRequest(orderRequest, orderObj);
        Order updated = orderRepository.update(orderObj);
        return mapper.orderToOrderResponse(updated);
    }
}
