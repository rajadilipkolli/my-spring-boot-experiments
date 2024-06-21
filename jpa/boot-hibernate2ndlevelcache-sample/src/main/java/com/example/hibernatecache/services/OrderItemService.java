package com.example.hibernatecache.services;

import com.example.hibernatecache.entities.OrderItem;
import com.example.hibernatecache.mapper.ConversionService;
import com.example.hibernatecache.model.response.OrderItemResponse;
import com.example.hibernatecache.model.response.PagedResult;
import com.example.hibernatecache.repositories.OrderItemRepository;
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
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final ConversionService mapper;

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

        return new PagedResult<>(orderItemsPage, orderItemResponses);
    }

    public Optional<OrderItemResponse> findOrderItemById(Long id) {
        return findById(id).map(mapper::orderItemToOrderItemResponse);
    }

    @Transactional
    public OrderItemResponse saveOrderItem(OrderItem orderItem) {
        OrderItem saved = orderItemRepository.persist(orderItem);
        return mapper.orderItemToOrderItemResponse(saved);
    }

    @Transactional
    public void deleteOrderItemById(Long id) {
        orderItemRepository.deleteById(id);
    }

    public Optional<OrderItem> findById(Long id) {
        return orderItemRepository.findById(id);
    }

    @Transactional
    public OrderItemResponse updateOrder(OrderItem orderObj) {
        OrderItem updated = orderItemRepository.update(orderObj);
        return mapper.orderItemToOrderItemResponse(updated);
    }
}
