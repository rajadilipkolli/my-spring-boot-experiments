package com.example.bootbatchjpa.config;

import static org.instancio.Select.field;

import com.example.bootbatchjpa.entities.Customer;
import com.example.bootbatchjpa.repositories.CustomerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Initializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");
        List<Customer> customerList = Instancio.ofList(Customer.class)
                .size(1000)
                .generate(field(Customer.class, "gender"), gen -> gen.oneOf("male", "female"))
                .create();
        log.info("Saving Customers of size :{}", customerList.size());
        customerList = customerRepository.saveAll(customerList);
        log.info("Inserted customers of size :{}", customerList.size());
    }
}
