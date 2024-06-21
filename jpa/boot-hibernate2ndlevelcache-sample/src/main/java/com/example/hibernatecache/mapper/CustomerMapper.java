package com.example.hibernatecache.mapper;

import com.example.hibernatecache.entities.Customer;
import com.example.hibernatecache.model.request.CustomerRequest;
import com.example.hibernatecache.model.response.CustomerResponse;
import java.util.List;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface CustomerMapper {

    Customer toEntity(CustomerRequest customerRequest);

    @IterableMapping(elementTargetType = CustomerResponse.class)
    List<CustomerResponse> toResponseList(List<Customer> customerList);

    @Mapping(source = "id", target = "customerId")
    CustomerResponse toResponse(Customer customer);

    void updateCustomerWithRequest(CustomerRequest customerRequest, @MappingTarget Customer savedCustomer);
}
