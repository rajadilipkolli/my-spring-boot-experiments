package com.example.featuretoggle.services;

import com.example.featuretoggle.config.logging.Loggable;
import com.example.featuretoggle.entities.Customer;
import com.example.featuretoggle.model.response.CustomerDTO;
import com.example.featuretoggle.repositories.CustomerRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.util.NamedFeature;

@Service
@Transactional
@Loggable
public class CustomerService {

    private final CustomerRepository customerRepository;

    private final FeatureManager featureManager;

    private static final Feature NAME = new NamedFeature("NAME");
    private static final Feature TEXT = new NamedFeature("TEXT");
    private static final Feature ZIP = new NamedFeature("ZIP");

    public CustomerService(CustomerRepository customerRepository, FeatureManager featureManager) {
        this.customerRepository = customerRepository;
        this.featureManager = featureManager;
    }

    @Transactional(readOnly = true)
    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<CustomerDTO> findCustomerById(Long id) {

        return customerRepository
                .findById(id)
                .map(
                        cust -> {
                            CustomerDTO customerDTO = new CustomerDTO();
                            customerDTO.setId(cust.getId());
                            if (featureManager.isActive(NAME)) {
                                customerDTO.setName(cust.getName());
                            }
                            if (featureManager.isActive(TEXT)) {
                                customerDTO.setText(cust.getText());
                            }
                            if (featureManager.isActive(ZIP)) {
                                customerDTO.setZipCode(cust.getZipCode());
                            }
                            return customerDTO;
                        });
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public void deleteCustomerById(Long id) {
        customerRepository.deleteById(id);
    }
}
