package com.example.envers.config;

import com.example.envers.entities.Customer;
import com.example.envers.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Initializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");
        Customer customerObj = new Customer();
        customerObj.setName("customerName");
        customerObj.setAddress("customerAddress");
        Customer persistedCustomer = this.customerRepository.save(customerObj);
        persistedCustomer.setName("updatedCustomer");
        Customer updatedCustomer = this.customerRepository.save(persistedCustomer);
        updatedCustomer.setName("updatedCustomer1");
        this.customerRepository.save(updatedCustomer);

        Revisions<Integer, Customer> revisions = this.customerRepository.findRevisions(persistedCustomer.getId());
        log.info("revisions ");
        for (Revision<Integer, Customer> content : revisions.getContent()) {
            log.info("Revision History Metadata :{}", content.getMetadata());
            log.info("Revision History Entry :{}", content.getEntity());
        }
    }
}
