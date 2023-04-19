package com.example.multitenancy.secondary.controller;

import com.example.multitenancy.secondary.entities.SecondaryCustomer;
import com.example.multitenancy.secondary.services.SecondaryCustomerService;
import com.example.multitenancy.utils.AppConstants;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.List;

@RestController
@RequestMapping("/api/customers/secondary")
@Slf4j
public class SecondaryCustomerController {

    private final SecondaryCustomerService secondaryCustomerService;

    @Autowired
    public SecondaryCustomerController(SecondaryCustomerService secondaryCustomerService) {
        this.secondaryCustomerService = secondaryCustomerService;
    }

    @GetMapping
    public List<SecondaryCustomer> getAllCustomers(
            @RequestHeader(AppConstants.X_TENANT_ID) String tenantId) {
        log.info("fetching all customer for tenant : {}", tenantId);
        return secondaryCustomerService.findAllCustomers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SecondaryCustomer> getCustomerById(
            @PathVariable Long id, @RequestHeader(AppConstants.X_TENANT_ID) String tenantId) {
        log.info("fetching customer by id {} for tenant : {}", id, tenantId);
        return secondaryCustomerService
                .findCustomerById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SecondaryCustomer createCustomer(
            @RequestBody @Validated SecondaryCustomer SecondaryCustomer,
            @RequestHeader(AppConstants.X_TENANT_ID) String tenantId) {
        log.info("creating customer by for tenant : {}", tenantId);
        return secondaryCustomerService.saveCustomer(SecondaryCustomer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SecondaryCustomer> updateCustomer(
            @PathVariable Long id,
            @RequestBody SecondaryCustomer SecondaryCustomer,
            @RequestHeader(AppConstants.X_TENANT_ID) String tenantId) {
        log.info("updating customer for id {} in tenant : {}", id, tenantId);
        return secondaryCustomerService
                .findCustomerById(id)
                .map(
                        customerObj -> {
                            SecondaryCustomer.setId(id);
                            return ResponseEntity.ok(
                                    secondaryCustomerService.saveCustomer(SecondaryCustomer));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SecondaryCustomer> deleteCustomer(
            @PathVariable Long id, @RequestHeader(AppConstants.X_TENANT_ID) String tenantId) {
        log.info("deleting customer by id {} for tenant : {}", id, tenantId);
        return secondaryCustomerService
                .findCustomerById(id)
                .map(
                        customer -> {
                            secondaryCustomerService.deleteCustomerById(id);
                            return ResponseEntity.ok(customer);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
