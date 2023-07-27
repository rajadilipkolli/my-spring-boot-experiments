package com.example.envers.services;

import com.example.envers.entities.Customer;
import com.example.envers.repositories.CustomerRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.history.Revisions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> findCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Revisions<Long, Customer> findCustomerRevisionsById(Long id) {
        return customerRepository.findRevisions(id);
    }

    @Transactional
    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    @Transactional
    public void deleteCustomerById(Long id) {
        customerRepository.deleteById(id);
    }
}
