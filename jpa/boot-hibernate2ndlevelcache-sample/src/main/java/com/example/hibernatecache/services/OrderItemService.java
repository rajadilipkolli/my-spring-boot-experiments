package com.example.hibernatecache.services;

import com.example.hibernatecache.entities.OrderItem;
import com.example.hibernatecache.exception.OrderItemNotFoundException;
import com.example.hibernatecache.mapper.OrderItemMapper;
import com.example.hibernatecache.model.query.FindOrderItemsQuery;
import com.example.hibernatecache.model.request.OrderItemRequest;
import com.example.hibernatecache.model.response.OrderItemResponse;
import com.example.hibernatecache.model.response.PagedResult;
import com.example.hibernatecache.repositories.OrderItemRepository;
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
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderItemMapper orderItemMapper;

    public OrderItemService(OrderItemRepository orderItemRepository, OrderItemMapper orderItemMapper) {
        this.orderItemRepository = orderItemRepository;
        this.orderItemMapper = orderItemMapper;
    }

    public PagedResult<OrderItemResponse> findAllOrderItems(FindOrderItemsQuery findOrderItemsQuery) {

        // create Pageable instance
        Pageable pageable = createPageable(findOrderItemsQuery);

        Page<OrderItem> orderItemsPage = orderItemRepository.findAll(pageable);

        List<OrderItemResponse> orderItemResponseList = orderItemMapper.toResponseList(orderItemsPage.getContent());

        return new PagedResult<>(orderItemsPage, orderItemResponseList);
    }

    private Pageable createPageable(FindOrderItemsQuery findOrderItemsQuery) {
        int pageNo = Math.max(findOrderItemsQuery.pageNo() - 1, 0);
        Sort sort = Sort.by(
                findOrderItemsQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.Order.asc(findOrderItemsQuery.sortBy())
                        : Sort.Order.desc(findOrderItemsQuery.sortBy()));
        return PageRequest.of(pageNo, findOrderItemsQuery.pageSize(), sort);
    }

    public Optional<OrderItemResponse> findOrderItemById(Long id) {
        return orderItemRepository.findById(id).map(orderItemMapper::toResponse);
    }

    @Transactional
    public OrderItemResponse updateOrderItem(Long id, OrderItemRequest orderItemRequest) {
        OrderItem orderItem = orderItemRepository.findById(id).orElseThrow(() -> new OrderItemNotFoundException(id));

        // Update the orderItem object with data from orderItemRequest
        orderItemMapper.mapOrderItemWithRequest(orderItemRequest, orderItem);

        // Save the updated orderItem object
        OrderItem updatedOrderItem = orderItemRepository.merge(orderItem);

        return orderItemMapper.toResponse(updatedOrderItem);
    }

    @Transactional
    public void deleteOrderItemById(Long id) {
        orderItemRepository.deleteById(id);
    }

    public List<OrderItemResponse> findOrderItemsByOrderId(Long orderId) {
        List<OrderItem> byOrderId = orderItemRepository.findByOrder_Id(orderId);
        return orderItemMapper.toResponseList(byOrderId);
    }
}
