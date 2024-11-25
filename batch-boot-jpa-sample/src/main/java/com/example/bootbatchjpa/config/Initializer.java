package com.example.bootbatchjpa.config;

import static org.instancio.Select.field;

import com.example.bootbatchjpa.entities.Customer;
import com.example.bootbatchjpa.repositories.CustomerRepository;
import java.util.List;
import org.instancio.Instancio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
class Initializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Initializer.class);
    private final CustomerRepository customerRepository;

    Initializer(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");
        List<Customer> customerList =
                Instancio.ofList(Customer.class)
                        .size(1000)
                        .generate(
                                field(Customer.class, "gender"), gen -> gen.oneOf("male", "female"))
                        .create();
        log.info("Saving Customers of size :{}", customerList.size());
        customerList = customerRepository.saveAllAndFlush(customerList);
        log.info("Inserted customers of size :{}", customerList.size());
    }
}
