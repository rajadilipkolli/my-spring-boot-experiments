package com.example.multitenancy.primary.services;

import com.example.multitenancy.primary.entities.PrimaryCustomer;
import com.example.multitenancy.primary.repositories.PrimaryCustomerRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(transactionManager = "primaryTransactionManager")
public class PrimaryCustomerService {

    private final PrimaryCustomerRepository primaryCustomerRepository;

    @Autowired
    public PrimaryCustomerService(PrimaryCustomerRepository primaryCustomerRepository) {
        this.primaryCustomerRepository = primaryCustomerRepository;
    }

    public List<PrimaryCustomer> findAllCustomers() {
        return primaryCustomerRepository.findAll();
    }

    public Optional<PrimaryCustomer> findCustomerById(Long id) {
        return primaryCustomerRepository.findById(id);
    }

    public PrimaryCustomer saveCustomer(PrimaryCustomer primaryCustomer) {
        return primaryCustomerRepository.save(primaryCustomer);
    }

    public void deleteCustomerById(Long id) {
        primaryCustomerRepository.deleteById(id);
    }
}
