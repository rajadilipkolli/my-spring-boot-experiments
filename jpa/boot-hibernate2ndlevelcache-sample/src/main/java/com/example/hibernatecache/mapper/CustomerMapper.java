package com.example.hibernatecache.mapper;

import com.example.hibernatecache.entities.Customer;
import com.example.hibernatecache.model.request.CustomerRequest;
import com.example.hibernatecache.model.response.CustomerResponse;
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
        uses = OrderMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        suppressTimestampInGenerated = true)
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    Customer toEntity(CustomerRequest customerRequest);

    @IterableMapping(elementTargetType = CustomerResponse.class)
    List<CustomerResponse> toResponseList(List<Customer> customerList);

    @Mapping(source = "id", target = "customerId")
    CustomerResponse toResponse(Customer customer);

    void updateCustomerWithRequest(CustomerRequest customerRequest, @MappingTarget Customer savedCustomer);
}
