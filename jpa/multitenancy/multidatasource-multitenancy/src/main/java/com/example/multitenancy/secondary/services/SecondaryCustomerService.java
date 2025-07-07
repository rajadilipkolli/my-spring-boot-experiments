package com.example.multitenancy.secondary.services;

import com.example.multitenancy.secondary.entities.SecondaryCustomer;
import com.example.multitenancy.secondary.model.request.SecondaryCustomerRequest;
import com.example.multitenancy.secondary.repositories.SecondaryCustomerRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(transactionManager = "secondaryTransactionManager", readOnly = true)
public class SecondaryCustomerService {

    private final SecondaryCustomerRepository secondaryCustomerRepository;

    public SecondaryCustomerService(SecondaryCustomerRepository secondaryCustomerRepository) {
        this.secondaryCustomerRepository = secondaryCustomerRepository;
    }

    public List<SecondaryCustomer> findAllCustomers() {
        return secondaryCustomerRepository.findAll();
    }

    public Optional<SecondaryCustomer> findCustomerById(Long id) {
        return secondaryCustomerRepository.findById(id);
    }

    @Transactional(transactionManager = "secondaryTransactionManager")
    public SecondaryCustomer saveCustomer(SecondaryCustomer customer) {
        return secondaryCustomerRepository.save(customer);
    }

    @Transactional(transactionManager = "secondaryTransactionManager")
    public SecondaryCustomer saveCustomer(SecondaryCustomerRequest secondaryCustomerRequest) {
        SecondaryCustomer secondaryCustomer = new SecondaryCustomer();
        secondaryCustomer.setName(secondaryCustomerRequest.name());
        return secondaryCustomerRepository.save(secondaryCustomer);
    }

    @Transactional(transactionManager = "secondaryTransactionManager")
    public void deleteCustomerById(Long id) {
        secondaryCustomerRepository.deleteById(id);
    }
}
