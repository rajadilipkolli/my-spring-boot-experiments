package com.example.multitenancy.primary.services;

import com.example.multitenancy.primary.entities.PrimaryCustomer;
import com.example.multitenancy.primary.model.request.PrimaryCustomerRequest;
import com.example.multitenancy.primary.repositories.PrimaryCustomerRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(transactionManager = "primaryTransactionManager", readOnly = true)
public class PrimaryCustomerService {

    private final PrimaryCustomerRepository primaryCustomerRepository;

    public PrimaryCustomerService(PrimaryCustomerRepository primaryCustomerRepository) {
        this.primaryCustomerRepository = primaryCustomerRepository;
    }

    public List<PrimaryCustomer> findAllCustomers() {
        return primaryCustomerRepository.findAll();
    }

    public Optional<PrimaryCustomer> findCustomerById(Long id) {
        return primaryCustomerRepository.findById(id);
    }

    @Transactional(transactionManager = "primaryTransactionManager")
    public PrimaryCustomer saveCustomer(PrimaryCustomerRequest primaryCustomer) {
        PrimaryCustomer customer = new PrimaryCustomer();
        customer.setText(primaryCustomer.text());
        return primaryCustomerRepository.save(customer);
    }

    @Transactional(transactionManager = "primaryTransactionManager")
    public PrimaryCustomer saveCustomer(PrimaryCustomer primaryCustomer) {
        return primaryCustomerRepository.save(primaryCustomer);
    }

    @Transactional(transactionManager = "primaryTransactionManager")
    public void deleteCustomerById(Long id) {
        primaryCustomerRepository.deleteById(id);
    }
}
