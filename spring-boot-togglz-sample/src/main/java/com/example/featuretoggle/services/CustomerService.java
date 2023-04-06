package com.example.featuretoggle.services;

import com.example.featuretoggle.entities.Customer;
import com.example.featuretoggle.repositories.CustomerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.util.NamedFeature;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    private final FeatureManager featureManager;

    public static final Feature ADD_NEW_FIELDS = new NamedFeature("ADD_NEW_FIELDS");

    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> findCustomerById(Long id) {
        if (featureManager.isActive(ADD_NEW_FIELDS)) {
            return Optional.empty();
        }
        return customerRepository.findById(id);
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public void deleteCustomerById(Long id) {
        customerRepository.deleteById(id);
    }
}
