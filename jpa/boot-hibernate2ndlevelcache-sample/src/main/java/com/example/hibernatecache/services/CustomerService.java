package com.example.hibernatecache.services;

import com.example.hibernatecache.entities.Customer;
import com.example.hibernatecache.exception.CustomerNotFoundException;
import com.example.hibernatecache.mapper.CustomerMapper;
import com.example.hibernatecache.model.query.FindCustomersQuery;
import com.example.hibernatecache.model.request.CustomerRequest;
import com.example.hibernatecache.model.response.CustomerResponse;
import com.example.hibernatecache.model.response.PagedResult;
import com.example.hibernatecache.repositories.CustomerRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    public PagedResult<CustomerResponse> findAllCustomers(FindCustomersQuery findCustomersQuery) {

        // create Pageable instance
        Pageable pageable = createPageable(findCustomersQuery);

        Page<Customer> customersPage = customerRepository.findAll(pageable);

        List<CustomerResponse> customerResponseList = customerMapper.toResponseList(customersPage.getContent());

        return new PagedResult<>(customersPage, customerResponseList);
    }

    private Pageable createPageable(FindCustomersQuery findCustomersQuery) {
        int pageNo = Math.max(findCustomersQuery.pageNo() - 1, 0);
        Sort sort = Sort.by(
                findCustomersQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.Order.asc(findCustomersQuery.sortBy())
                        : Sort.Order.desc(findCustomersQuery.sortBy()));
        return PageRequest.of(pageNo, findCustomersQuery.pageSize(), sort);
    }

    public boolean existsById(Long id) {
        return customerRepository.existsById(id);
    }

    public Optional<CustomerResponse> findCustomerById(Long id) {
        return customerRepository.findById(id).map(customerMapper::toResponse);
    }

    public Optional<CustomerResponse> findCustomerByFirstName(String firstName) {
        return customerRepository.findByFirstName(firstName).map(customerMapper::toResponse);
    }

    @Transactional
    public CustomerResponse saveCustomer(CustomerRequest customerRequest) {
        Customer customer = customerMapper.toEntity(customerRequest);
        Customer savedCustomer = customerRepository.persist(customer);
        return customerMapper.toResponse(savedCustomer);
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest customerRequest) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));

        // Update the customer object with data from customerRequest
        customerMapper.updateCustomerWithRequest(customerRequest, customer);

        // Save the updated customer object
        Customer updatedCustomer = customerRepository.merge(customer);

        return customerMapper.toResponse(updatedCustomer);
    }

    @Transactional
    public void deleteCustomerById(Long id) {
        customerRepository.deleteById(id);
    }
}
