package com.example.hibernatecache.mapper;

import com.example.hibernatecache.entities.Order;
import com.example.hibernatecache.entities.OrderItem;
import com.example.hibernatecache.model.request.OrderItemRequest;
import com.example.hibernatecache.model.request.OrderRequest;
import com.example.hibernatecache.model.response.OrderResponse;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.util.CollectionUtils;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        uses = OrderItemMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        suppressTimestampInGenerated = true)
public interface OrderMapper {

    @Mapping(target = "removeOrderItem", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer.id", source = "customerId")
    @Mapping(target = "price", ignore = true)
    Order toEntity(OrderRequest orderRequest);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(source = "id", target = "orderId")
    OrderResponse toResponse(Order order);

    @IterableMapping(elementTargetType = OrderResponse.class)
    List<OrderResponse> mapToOrderResponseList(List<Order> orderList);

    @Mapping(target = "removeOrderItem", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "price", ignore = true)
    void updateOrderWithRequest(OrderRequest orderRequest, @MappingTarget Order savedOrder);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    OrderItem toOrderItemEntity(OrderItemRequest orderItemRequest);

    @AfterMapping
    default void mapOrderItemWithRequestAndCalculateTotalPrice(OrderRequest orderRequest, @MappingTarget Order order) {
        AtomicReference<BigDecimal> totalPrice;
        Set<String> existingItemCodes;
        if (CollectionUtils.isEmpty(order.getOrderItems())) {
            existingItemCodes = new HashSet<>();
            totalPrice = new AtomicReference<>(BigDecimal.ZERO);
        } else {
            totalPrice = new AtomicReference<>(order.getPrice());
            existingItemCodes =
                    order.getOrderItems().stream().map(OrderItem::getItemCode).collect(Collectors.toSet());
        }

        orderRequest.orderItems().forEach(orderItemRequest -> {
            if (existingItemCodes.contains(orderItemRequest.itemCode())) {
                return;
            }
            order.addOrderItem(toOrderItemEntity(orderItemRequest));
            BigDecimal price = orderItemRequest.price().multiply(BigDecimal.valueOf(orderItemRequest.quantity()));
            totalPrice.updateAndGet(current -> current.add(price));
        });
        order.setPrice(totalPrice.get());
    }
}
