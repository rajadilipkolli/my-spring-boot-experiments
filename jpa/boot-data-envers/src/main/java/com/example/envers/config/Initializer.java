package com.example.envers.config;

import com.example.envers.entities.Customer;
import com.example.envers.repositories.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.springframework.stereotype.Component;

@Component
public class Initializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);

    private final CustomerRepository customerRepository;

    public Initializer(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public void run(String... args) {
        LOGGER.info("Running Initializer.....");
        Customer customerObj = new Customer();
        customerObj.setName("customerName");
        customerObj.setAddress("customerAddress");
        Customer persistedCustomer = this.customerRepository.save(customerObj);
        persistedCustomer.setName("updatedCustomer");
        Customer updatedCustomer = this.customerRepository.save(persistedCustomer);
        updatedCustomer.setName("updatedCustomer1");
        this.customerRepository.save(updatedCustomer);

        Revisions<Integer, Customer> revisions = this.customerRepository.findRevisions(persistedCustomer.getId());
        LOGGER.info("revisions ");
        for (Revision<Integer, Customer> content : revisions.getContent()) {
            LOGGER.info("Revision History Metadata :{}", content.getMetadata());
            LOGGER.info("Revision History Entry :{}", content.getEntity());
        }
    }
}
