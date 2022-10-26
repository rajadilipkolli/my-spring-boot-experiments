package com.example.multitenancy.schema.mapper;

import com.example.multitenancy.schema.domain.request.CustomerDto;
import com.example.multitenancy.schema.entities.Customer;
import java.util.List;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerDto entityToDTO(Customer project);

    @IterableMapping
    List<CustomerDto> entityToDTO(Iterable<Customer> project);

    @InheritInverseConfiguration
    Customer dtoToEntity(CustomerDto project);

    @IterableMapping
    List<Customer> dtoToEntity(Iterable<CustomerDto> projects);
}
