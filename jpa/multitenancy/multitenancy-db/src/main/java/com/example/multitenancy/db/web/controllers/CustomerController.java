package com.example.multitenancy.db.web.controllers;

import com.example.multitenancy.db.entities.Customer;
import com.example.multitenancy.db.services.CustomerService;
import com.example.multitenancy.db.utils.AppConstants;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    List<Customer> getAllCustomers(@RequestHeader(AppConstants.X_TENANT_ID) String tenantId) {
        log.info("fetching all customer for tenant : {}", tenantId);
        return customerService.findAllCustomers();
    }

    @GetMapping("/{id}")
    ResponseEntity<Customer> getCustomerById(
            @PathVariable Long id, @RequestHeader(AppConstants.X_TENANT_ID) String tenantId) {
        log.info("fetching customer by id {} for tenant : {}", id, tenantId);
        return customerService.findCustomerById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound()
                .build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Customer createCustomer(
            @RequestBody @Validated Customer customer, @RequestHeader(AppConstants.X_TENANT_ID) String tenantId) {
        log.info("creating customer by for tenant : {}", tenantId);
        return customerService.saveCustomer(customer);
    }

    @PutMapping("/{id}")
    ResponseEntity<Customer> updateCustomer(
            @PathVariable Long id,
            @RequestBody Customer customer,
            @RequestHeader(AppConstants.X_TENANT_ID) String tenantId) {
        log.info("updating customer for id {} in tenant : {}", id, tenantId);
        return customerService
                .findCustomerById(id)
                .map(customerObj -> {
                    customer.setId(id);
                    return ResponseEntity.ok(customerService.saveCustomer(customer));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Customer> deleteCustomer(
            @PathVariable Long id, @RequestHeader(AppConstants.X_TENANT_ID) String tenantId) {
        log.info("deleting customer by id {} for tenant : {}", id, tenantId);
        return customerService
                .findCustomerById(id)
                .map(customer -> {
                    customerService.deleteCustomerById(id);
                    return ResponseEntity.ok(customer);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
