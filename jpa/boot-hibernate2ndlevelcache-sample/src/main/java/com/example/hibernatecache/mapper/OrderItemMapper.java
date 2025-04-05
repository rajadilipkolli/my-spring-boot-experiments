package com.example.hibernatecache.mapper;

import com.example.hibernatecache.entities.OrderItem;
import com.example.hibernatecache.model.request.OrderItemRequest;
import com.example.hibernatecache.model.response.OrderItemResponse;
import java.util.List;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        suppressTimestampInGenerated = true)
public interface OrderItemMapper {

    @Mapping(source = "id", target = "orderItemId")
    OrderItemResponse toResponse(OrderItem orderItem);

    @IterableMapping(elementTargetType = OrderItemResponse.class)
    List<OrderItemResponse> toResponseList(List<OrderItem> list);

    @Mapping(target = "order", ignore = true)
    void mapOrderItemWithRequest(OrderItemRequest orderItemRequest, @MappingTarget OrderItem orderItem);
}
