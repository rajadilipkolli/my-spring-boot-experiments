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
        Customer customerObj = new Customer("customerName");
        Customer persistedCustomer = this.customerRepository.save(customerObj);
        persistedCustomer.setName("updatedCustomer");
        this.customerRepository.save(persistedCustomer);

        Revisions<Long, Customer> revisions = this.customerRepository.findRevisions(persistedCustomer.getId());
        log.info("revisions ");
        for (Revision<Long, Customer> content : revisions.getContent()) {
            log.info("Revision History Metadata :{}", content.getMetadata());
            log.info("Revision History Entry :{}", content.getEntity());
        }
    }
}
