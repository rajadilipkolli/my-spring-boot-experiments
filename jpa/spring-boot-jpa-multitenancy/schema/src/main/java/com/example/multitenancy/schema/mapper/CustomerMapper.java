package com.example.multitenancy.schema.mapper;

import com.example.multitenancy.schema.domain.request.CustomerDto;
import com.example.multitenancy.schema.entities.Customer;
import java.util.List;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface CustomerMapper {

    CustomerDto entityToDTO(Customer customer);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<CustomerDto> entityToDTO(Iterable<Customer> customerEntityList);

    @InheritInverseConfiguration
    Customer dtoToEntity(CustomerDto customerDto);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<Customer> dtoToEntity(Iterable<CustomerDto> customerDtoList);

    Customer updateCustomerFromDto(CustomerDto customerDto, @MappingTarget Customer customer);
}
