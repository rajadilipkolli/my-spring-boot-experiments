package com.example.multitenancy.schema.services;

import com.example.multitenancy.schema.config.TenantIdentifierResolver;
import com.example.multitenancy.schema.entities.Customer;
import com.example.multitenancy.schema.repositories.CustomerRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final TenantIdentifierResolver tenantIdentifierResolver;

    @Autowired
    public CustomerService(
            CustomerRepository customerRepository,
            TenantIdentifierResolver tenantIdentifierResolver) {
        this.customerRepository = customerRepository;
        this.tenantIdentifierResolver = tenantIdentifierResolver;
    }

    public List<Customer> findAllCustomers(String tenant) {
        tenantIdentifierResolver.setCurrentTenant(tenant);
        return customerRepository.findAll();
    }

    public Optional<Customer> findCustomerById(Long id, String tenant) {
        tenantIdentifierResolver.setCurrentTenant(tenant);
        return customerRepository.findById(id);
    }

    public Customer saveCustomer(Customer customer, String tenant) {
        tenantIdentifierResolver.setCurrentTenant(tenant);
        return customerRepository.save(customer);
    }

    public void deleteCustomerById(Long id, String tenant) {
        tenantIdentifierResolver.setCurrentTenant(tenant);
        customerRepository.deleteById(id);
    }
}
