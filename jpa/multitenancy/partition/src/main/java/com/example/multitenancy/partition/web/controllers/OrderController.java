package com.example.multitenancy.partition.web.controllers;

import com.example.multitenancy.partition.dto.OrderRequestDTO;
import com.example.multitenancy.partition.entities.Order;
import com.example.multitenancy.partition.services.OrderService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.findAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService
                .findOrderById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        Order savedOrder = orderService.saveOrder(orderRequestDTO);
        return ResponseEntity.created(URI.create("/api/orders/" + savedOrder.getId()))
                .body(savedOrder);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(
            @PathVariable Long id, @Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        return orderService
                .findOrderById(id)
                .map(
                        existingOrder -> {
                            existingOrder.setAmount(orderRequestDTO.amount());
                            existingOrder.setOrderDate(orderRequestDTO.orderDate());
                            return ResponseEntity.ok(orderService.saveOrder(existingOrder));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteOrder(@PathVariable Long id) {
        return orderService
                .findOrderById(id)
                .map(
                        existingOrder -> {
                            orderService.deleteOrderById(id);
                            return ResponseEntity.ok().build();
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
