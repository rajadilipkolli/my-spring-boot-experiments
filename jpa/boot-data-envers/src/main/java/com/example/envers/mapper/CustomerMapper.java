package com.example.envers.mapper;

import com.example.envers.entities.Customer;
import com.example.envers.model.request.CustomerRequest;
import com.example.envers.model.response.CustomerResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CustomerMapper {

    public Customer toEntity(CustomerRequest customerRequest) {
        Customer customer = new Customer();
        customer.setName(customerRequest.name());
        customer.setAddress(customerRequest.address());
        return customer;
    }

    public void mapCustomerWithRequest(Customer customer, CustomerRequest customerRequest) {
        customer.setName(customerRequest.name());
        customer.setAddress(customerRequest.address());
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getName(), customer.getAddress());
    }

    public List<CustomerResponse> toResponseList(List<Customer> customerList) {
        return customerList.stream().map(this::toResponse).toList();
    }
}
