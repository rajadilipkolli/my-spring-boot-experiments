package com.example.hibernatecache.web.controllers;

import com.example.hibernatecache.entities.OrderItem;
import com.example.hibernatecache.model.response.OrderItemResponse;
import com.example.hibernatecache.model.response.PagedResult;
import com.example.hibernatecache.services.OrderItemService;
import com.example.hibernatecache.utils.AppConstants;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService orderItemService;

    @GetMapping
    public PagedResult<OrderItemResponse> getAllOrderItems(
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
    public ResponseEntity<OrderItemResponse> getOrderItemById(@PathVariable Long id) {
        return orderItemService
                .findOrderItemById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderItemResponse createOrderItem(@RequestBody @Validated OrderItem orderItem) {
        return orderItemService.saveOrderItem(orderItem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderItemResponse> updateOrderItem(
            @PathVariable Long id, @RequestBody OrderItem orderItem) {
        return orderItemService
                .findById(id)
                .map(
                        orderItemObj -> {
                            orderItem.setId(id);
                            return ResponseEntity.ok(orderItemService.updateOrder(orderItem));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderItemResponse> deleteOrderItem(@PathVariable Long id) {
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
