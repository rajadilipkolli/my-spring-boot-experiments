package com.example.hibernatecache.services;

import com.example.hibernatecache.entities.Customer;
import com.example.hibernatecache.mapper.Mapper;
import com.example.hibernatecache.model.response.CustomerResponse;
import com.example.hibernatecache.model.response.PagedResult;
import com.example.hibernatecache.repositories.CustomerRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final Mapper mapper;

    public PagedResult<CustomerResponse> findAllCustomers(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Customer> customersPage = customerRepository.findAll(pageable);
        List<CustomerResponse> customerResponses =
                mapper.mapToCustomerResponseList(customersPage.getContent());
        Page<CustomerResponse> customerResponsePage =
                new PageImpl<>(customerResponses, pageable, customersPage.getTotalElements());
        return new PagedResult<>(customerResponsePage);
    }

    public Optional<CustomerResponse> findCustomerById(Long id) {
        return findById(id).map(mapper::mapToCustomerResponse);
    }

    @Transactional
    public CustomerResponse saveCustomer(Customer customer) {
        Customer saved = customerRepository.persist(customer);
        return mapper.mapToCustomerResponse(saved);
    }

    @Transactional
    public void deleteCustomerById(Long id) {
        customerRepository.deleteById(id);
    }

    public Optional<CustomerResponse> findCustomerByFirstName(String firstName) {
        return customerRepository.findByFirstName(firstName).map(mapper::mapToCustomerResponse);
    }

    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    @Transactional
    public CustomerResponse updateCustomer(Customer customerRequest, Customer savedCustomer) {
        mapper.updateCustomerWithRequest(customerRequest, savedCustomer);
        Customer updated = customerRepository.update(savedCustomer);
        return mapper.mapToCustomerResponse(updated);
    }
}
