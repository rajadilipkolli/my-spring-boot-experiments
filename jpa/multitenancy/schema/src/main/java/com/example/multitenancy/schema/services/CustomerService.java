package com.example.multitenancy.schema.services;

import com.example.multitenancy.schema.domain.request.CustomerDto;
import com.example.multitenancy.schema.entities.Customer;
import com.example.multitenancy.schema.mapper.CustomerMapper;
import com.example.multitenancy.schema.repositories.CustomerRepository;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    private final CustomerMapper customerMapper;

    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> findCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Customer saveCustomer(CustomerDto customer) {
        return customerRepository.save(customerMapper.dtoToEntity(customer));
    }

    public Optional<Customer> updateCustomer(long id, CustomerDto customerDto) {
        final Function<Customer, Customer> customerResponseEntityFunction =
                customerObj ->
                        customerRepository.save(
                                customerMapper.updateCustomerFromDto(customerDto, customerObj));
        return findCustomerById(id).map(customerResponseEntityFunction);
    }

    public void deleteCustomerById(Long id) {
        customerRepository.deleteById(id);
    }
}
