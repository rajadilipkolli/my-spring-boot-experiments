package com.example.choasmonkey.web.controllers;

import com.example.choasmonkey.entities.Customer;
import com.example.choasmonkey.model.response.CustomerResponse;
import com.example.choasmonkey.services.CustomerService;
import com.example.choasmonkey.utils.AppConstants;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final ObservationRegistry observationRegistry;

    public CustomerController(CustomerService customerService, ObservationRegistry observationRegistry) {
        this.customerService = customerService;
        this.observationRegistry = observationRegistry;
    }

    @GetMapping
    public CustomerResponse getAllCustomers(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        return customerService.findAllCustomers(pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {

        return observerRequest(
                "customers.findById",
                customerService.findCustomerById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound()
                        .build()));
    }

    private ResponseEntity<Customer> observerRequest(
            String metricName, ResponseEntity<Customer> customerResponseEntity) {
        return Observation.createNotStarted(metricName, observationRegistry).observe(() -> customerResponseEntity);
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(
            @RequestBody @Validated Customer customer, UriComponentsBuilder uriComponentsBuilder) {
        Customer createdCustomer = customerService.saveCustomer(customer);
        return observerRequest(
                "customers.create",
                ResponseEntity.created(uriComponentsBuilder
                                .path("/api/customers/{id}")
                                .buildAndExpand(createdCustomer.getId())
                                .toUri())
                        .body(createdCustomer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        return observerRequest(
                "customers.update",
                customerService
                        .findCustomerById(id)
                        .map(customerObj -> {
                            customer.setId(id);
                            return ResponseEntity.ok(customerService.saveCustomer(customer));
                        })
                        .orElseGet(() -> ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Customer> deleteCustomer(@PathVariable Long id) {
        return observerRequest(
                "customers.delete",
                customerService
                        .findCustomerById(id)
                        .map(customer -> {
                            customerService.deleteCustomerById(id);
                            return ResponseEntity.ok(customer);
                        })
                        .orElseGet(() -> ResponseEntity.notFound().build()));
    }
}
