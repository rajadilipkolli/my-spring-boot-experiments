package com.example.hibernatecache.web.controllers;

import com.example.hibernatecache.exception.OrderItemNotFoundException;
import com.example.hibernatecache.model.query.FindOrderItemsQuery;
import com.example.hibernatecache.model.request.OrderItemRequest;
import com.example.hibernatecache.model.response.OrderItemResponse;
import com.example.hibernatecache.model.response.PagedResult;
import com.example.hibernatecache.services.OrderItemService;
import com.example.hibernatecache.utils.AppConstants;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order/items")
class OrderItemController {

    private final OrderItemService orderItemService;

    OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @GetMapping
    PagedResult<OrderItemResponse> getAllOrderItems(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        FindOrderItemsQuery findOrderItemsQuery = new FindOrderItemsQuery(pageNo, pageSize, sortBy, sortDir);
        return orderItemService.findAllOrderItems(findOrderItemsQuery);
    }

    @GetMapping("/{id}")
    ResponseEntity<OrderItemResponse> getOrderItemById(@PathVariable Long id) {
        return orderItemService
                .findOrderItemById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new OrderItemNotFoundException(id));
    }

    @PutMapping("/{id}")
    ResponseEntity<OrderItemResponse> updateOrderItem(
            @PathVariable Long id, @RequestBody @Valid OrderItemRequest orderItemRequest) {
        return ResponseEntity.ok(orderItemService.updateOrderItem(id, orderItemRequest));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<OrderItemResponse> deleteOrderItem(@PathVariable Long id) {
        return orderItemService
                .findOrderItemById(id)
                .map(orderItem -> {
                    orderItemService.deleteOrderItemById(id);
                    return ResponseEntity.ok(orderItem);
                })
                .orElseThrow(() -> new OrderItemNotFoundException(id));
    }
}
