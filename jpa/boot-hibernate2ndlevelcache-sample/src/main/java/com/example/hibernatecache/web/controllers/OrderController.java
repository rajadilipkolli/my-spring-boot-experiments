package com.example.hibernatecache.web.controllers;

import com.example.hibernatecache.exception.OrderNotFoundException;
import com.example.hibernatecache.model.query.FindOrdersQuery;
import com.example.hibernatecache.model.request.OrderRequest;
import com.example.hibernatecache.model.response.OrderResponse;
import com.example.hibernatecache.model.response.PagedResult;
import com.example.hibernatecache.services.OrderService;
import com.example.hibernatecache.utils.AppConstants;
import jakarta.validation.Valid;
import java.net.URI;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/orders")
class OrderController {

    private final OrderService orderService;

    OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    PagedResult<OrderResponse> getAllOrders(
            @RequestParam(value = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false)
                    int pageNo,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false)
                    int pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false)
                    String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false)
                    String sortDir) {
        FindOrdersQuery findOrdersQuery = new FindOrdersQuery(pageNo, pageSize, sortBy, sortDir);
        return orderService.findAllOrders(findOrdersQuery);
    }

    @GetMapping("/{id}")
    ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return orderService.findOrderById(id).map(ResponseEntity::ok).orElseThrow(() -> new OrderNotFoundException(id));
    }

    @PostMapping
    ResponseEntity<OrderResponse> createOrder(@RequestBody @Validated OrderRequest orderRequest) {
        OrderResponse response = orderService.saveOrder(orderRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/api/orders/{id}")
                .buildAndExpand(response.orderId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    ResponseEntity<OrderResponse> updateOrder(@PathVariable Long id, @RequestBody @Valid OrderRequest orderRequest) {
        return ResponseEntity.ok(orderService.updateOrder(id, orderRequest));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<OrderResponse> deleteOrder(@PathVariable Long id) {
        return orderService
                .findOrderById(id)
                .map(order -> {
                    orderService.deleteOrderById(id);
                    return ResponseEntity.ok(order);
                })
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
