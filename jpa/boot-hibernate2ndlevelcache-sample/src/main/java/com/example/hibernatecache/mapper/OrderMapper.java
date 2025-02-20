package com.example.hibernatecache.mapper;

import com.example.hibernatecache.entities.Order;
import com.example.hibernatecache.entities.OrderItem;
import com.example.hibernatecache.model.request.OrderItemRequest;
import com.example.hibernatecache.model.request.OrderRequest;
import com.example.hibernatecache.model.response.OrderResponse;
import java.math.BigDecimal;
import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;

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
    void updateOrderWithRequest(OrderRequest orderRequest, @MappingTarget Order savedOrder);

    @Mapping(target = "id", ignore = true)
    OrderItem toOrderItemEntity(OrderItemRequest orderItemRequest);

    @AfterMapping
    default void mapOrderItemWithRequestAndCalculateTotalPrice(OrderRequest orderRequest, @MappingTarget Order order) {
        var holder = new Object() {
            BigDecimal totalPrice = BigDecimal.ZERO;
        };
        orderRequest.orderItems().forEach(orderItemRequest -> {
            order.addOrderItem(toOrderItemEntity(orderItemRequest));
            BigDecimal price = orderItemRequest.price().multiply(BigDecimal.valueOf(orderItemRequest.quantity()));
            holder.totalPrice = holder.totalPrice.add(price);
        });
        order.setPrice(holder.totalPrice);
    }
}
