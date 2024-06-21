package com.example.hibernatecache.mapper;

import com.example.hibernatecache.entities.Order;
import com.example.hibernatecache.model.request.OrderRequest;
import com.example.hibernatecache.model.response.OrderResponse;
import java.util.List;
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

    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer.id", source = "customerId")
    Order toEntity(OrderRequest orderRequest);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(source = "id", target = "orderId")
    OrderResponse toResponse(Order order);

    @IterableMapping(elementTargetType = OrderResponse.class)
    List<OrderResponse> mapToOrderResponseList(List<Order> orderList);

    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    void updateOrderWithRequest(OrderRequest orderRequest, @MappingTarget Order savedOrder);
}
