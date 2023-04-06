package com.example.custom.sequence.services;

import com.example.custom.sequence.entities.Customer;
import com.example.custom.sequence.model.response.PagedResult;
import com.example.custom.sequence.repositories.CustomerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public PagedResult<Customer> findAllCustomers(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Customer> customersPage = customerRepository.findAll(pageable);

        return new PagedResult<>(customersPage);
    }

    public Optional<Customer> findCustomerById(String id) {
        return customerRepository.findById(id);
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public void deleteCustomerById(String id) {
        customerRepository.deleteById(id);
    }
}
