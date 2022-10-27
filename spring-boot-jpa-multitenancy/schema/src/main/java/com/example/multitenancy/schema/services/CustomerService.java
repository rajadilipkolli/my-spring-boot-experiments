package com.example.multitenancy.schema.services;

import com.example.multitenancy.schema.domain.request.CustomerDto;
import com.example.multitenancy.schema.entities.Customer;
import com.example.multitenancy.schema.mapper.CustomerMapper;
import com.example.multitenancy.schema.repositories.CustomerRepository;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
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

    public ResponseEntity<Customer> updateCustomer(long id, CustomerDto customerDto) {
        Function<Customer, ResponseEntity<Customer>> customerResponseEntityFunction =
                customerObj ->
                        ResponseEntity.ok(
                                customerRepository.save(
                                        customerMapper.updateCustomerFromDto(
                                                customerDto, customerObj)));
        return findCustomerById(id)
                .map(customerResponseEntityFunction)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public void deleteCustomerById(Long id) {
        customerRepository.deleteById(id);
    }
}
