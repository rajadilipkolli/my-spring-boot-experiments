package com.example.multitenancy.secondary.services;

import com.example.multitenancy.secondary.entities.SecondaryCustomer;
import com.example.multitenancy.secondary.repositories.SecondaryCustomerRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(transactionManager = "secondaryTransactionManager")
public class SecondaryCustomerService {

    private final SecondaryCustomerRepository secondaryCustomerRepository;

    @Autowired
    public SecondaryCustomerService(SecondaryCustomerRepository secondaryCustomerRepository) {
        this.secondaryCustomerRepository = secondaryCustomerRepository;
    }

    public List<SecondaryCustomer> findAllCustomers() {
        return secondaryCustomerRepository.findAll();
    }

    public Optional<SecondaryCustomer> findCustomerById(Long id) {
        return secondaryCustomerRepository.findById(id);
    }

    public SecondaryCustomer saveCustomer(SecondaryCustomer customer) {
        return secondaryCustomerRepository.save(customer);
    }

    public void deleteCustomerById(Long id) {
        secondaryCustomerRepository.deleteById(id);
    }
}
