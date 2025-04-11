package com.example.multitenancy.partition.web.controllers;

import com.example.multitenancy.partition.entities.Order;
import com.example.multitenancy.partition.services.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.findAllOrders();
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
    public Order createOrder(@RequestBody Order order) {
        return orderService.saveOrder(order);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order order) {
        return orderService
                .findOrderById(id)
                .map(
                        existingOrder -> {
                            order.setId(id);
                            return ResponseEntity.ok(orderService.saveOrder(order));
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
