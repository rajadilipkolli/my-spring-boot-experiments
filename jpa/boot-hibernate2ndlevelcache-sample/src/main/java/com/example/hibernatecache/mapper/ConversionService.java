package com.example.hibernatecache.mapper;

import com.example.hibernatecache.entities.Customer;
import com.example.hibernatecache.entities.Order;
import com.example.hibernatecache.entities.OrderItem;
import com.example.hibernatecache.model.request.OrderRequest;
import com.example.hibernatecache.model.response.CustomerResponse;
import com.example.hibernatecache.model.response.OrderItemResponse;
import com.example.hibernatecache.model.response.OrderResponse;
import java.util.List;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ConversionService {

    @Mapping(source = "id", target = "customerId")
    CustomerResponse mapToCustomerResponse(Customer customer);

    @IterableMapping(elementTargetType = CustomerResponse.class)
    List<CustomerResponse> mapToCustomerResponseList(List<Customer> customerList);

    void updateCustomerWithRequest(Customer customerRequest, @MappingTarget Customer savedCustomer);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(source = "id", target = "orderId")
    OrderResponse orderToOrderResponse(Order order);

    @IterableMapping(elementTargetType = OrderResponse.class)
    List<OrderResponse> mapToOrderResponseList(List<Order> orderList);

    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    void updateOrderWithRequest(OrderRequest orderRequest, @MappingTarget Order savedOrder);

    @Mapping(source = "id", target = "orderItemId")
    OrderItemResponse orderItemToOrderItemResponse(OrderItem orderItem);

    @IterableMapping(elementTargetType = OrderItemResponse.class)
    List<OrderItemResponse> orderItemListToOrderItemResponseList(List<OrderItem> list);

    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer.id", source = "customerId")
    Order mapToOrder(OrderRequest orderRequest);
}
