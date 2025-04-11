package com.example.multitenancy.partition.services;

import com.example.multitenancy.partition.dto.OrderRequestDTO;
import com.example.multitenancy.partition.entities.Order;
import com.example.multitenancy.partition.repositories.OrderRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> findOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional
    public Order saveOrder(OrderRequestDTO order) {
        Order orderEntity = new Order();
        orderEntity.setAmount(order.amount());
        orderEntity.setOrderDate(order.orderDate());

        return orderRepository.save(orderEntity);
    }

    @Transactional
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    @Transactional
    public void deleteOrderById(Long id) {
        orderRepository.deleteById(id);
    }
}
