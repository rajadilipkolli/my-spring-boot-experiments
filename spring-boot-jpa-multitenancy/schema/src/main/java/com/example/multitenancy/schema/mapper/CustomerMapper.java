package com.example.multitenancy.schema.mapper;

import com.example.multitenancy.schema.domain.request.CustomerDto;
import com.example.multitenancy.schema.entities.Customer;
import java.util.List;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerDto entityToDTO(Customer project);

    List<CustomerDto> entityToDTO(Iterable<Customer> customerEntityList);

    @InheritInverseConfiguration
    Customer dtoToEntity(CustomerDto project);

    List<Customer> dtoToEntity(Iterable<CustomerDto> customerDtoList);
}
