package com.example.hibernatecache.services;

import com.example.hibernatecache.entities.OrderItem;
import com.example.hibernatecache.mapper.Mapper;
import com.example.hibernatecache.model.response.OrderItemResponse;
import com.example.hibernatecache.model.response.PagedResult;
import com.example.hibernatecache.repositories.OrderItemRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final Mapper mapper;

    @Autowired
    public OrderItemService(OrderItemRepository orderItemRepository, Mapper mapper) {
        this.orderItemRepository = orderItemRepository;
        this.mapper = mapper;
    }

    public PagedResult<OrderItemResponse> findAllOrderItems(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<OrderItem> orderItemsPage = orderItemRepository.findAll(pageable);
        List<OrderItemResponse> orderItemResponses =
                mapper.orderItemListToOrderItemResponseList(orderItemsPage.getContent());

        return new PagedResult<>(
                new PageImpl<>(orderItemResponses, pageable, orderItemsPage.getTotalElements()));
    }

    public Optional<OrderItemResponse> findOrderItemById(Long id) {
        return findById(id).map(mapper::orderItemToOrderItemResponse);
    }

    public OrderItemResponse saveOrderItem(OrderItem orderItem) {
        OrderItem saved = orderItemRepository.save(orderItem);
        return mapper.orderItemToOrderItemResponse(saved);
    }

    public void deleteOrderItemById(Long id) {
        orderItemRepository.deleteById(id);
    }

    public Optional<OrderItem> findById(Long id) {
        return orderItemRepository.findById(id);
    }
}
