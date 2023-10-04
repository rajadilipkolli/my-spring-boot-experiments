package com.example.hibernatecache.web.controllers;

import com.example.hibernatecache.entities.OrderItem;
import com.example.hibernatecache.model.response.PagedResult;
import com.example.hibernatecache.services.OrderItemService;
import com.example.hibernatecache.utils.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order/items")
@Slf4j
public class OrderItemController {

    private final OrderItemService orderItemService;

    @Autowired
    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @GetMapping
    public PagedResult<OrderItem> getAllOrderItems(
            @RequestParam(
                            value = "pageNo",
                            defaultValue = AppConstants.DEFAULT_PAGE_NUMBER,
                            required = false)
                    int pageNo,
            @RequestParam(
                            value = "pageSize",
                            defaultValue = AppConstants.DEFAULT_PAGE_SIZE,
                            required = false)
                    int pageSize,
            @RequestParam(
                            value = "sortBy",
                            defaultValue = AppConstants.DEFAULT_SORT_BY,
                            required = false)
                    String sortBy,
            @RequestParam(
                            value = "sortDir",
                            defaultValue = AppConstants.DEFAULT_SORT_DIRECTION,
                            required = false)
                    String sortDir) {
        return orderItemService.findAllOrderItems(pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderItem> getOrderItemById(@PathVariable Long id) {
        return orderItemService
                .findOrderItemById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderItem createOrderItem(@RequestBody @Validated OrderItem orderItem) {
        return orderItemService.saveOrderItem(orderItem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderItem> updateOrderItem(
            @PathVariable Long id, @RequestBody OrderItem orderItem) {
        return orderItemService
                .findOrderItemById(id)
                .map(
                        orderItemObj -> {
                            orderItem.setId(id);
                            return ResponseEntity.ok(orderItemService.saveOrderItem(orderItem));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderItem> deleteOrderItem(@PathVariable Long id) {
        return orderItemService
                .findOrderItemById(id)
                .map(
                        orderItem -> {
                            orderItemService.deleteOrderItemById(id);
                            return ResponseEntity.ok(orderItem);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
