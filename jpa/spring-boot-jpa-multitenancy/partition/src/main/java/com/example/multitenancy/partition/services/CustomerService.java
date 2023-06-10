package com.example.multitenancy.partition.services;

import com.example.multitenancy.partition.dto.CustomerDTO;
import com.example.multitenancy.partition.entities.Customer;
import com.example.multitenancy.partition.repositories.CustomerRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> findCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public Customer saveCustomer(CustomerDTO customerDTO) {
        return customerRepository.save(maptoEntity(customerDTO));
    }

    private Customer maptoEntity(CustomerDTO customerDTO) {
        Customer customer = new Customer();
        customer.setText(customerDTO.text());
        return customer;
    }

    public void deleteCustomerById(Long id) {
        customerRepository.deleteById(id);
    }
}
