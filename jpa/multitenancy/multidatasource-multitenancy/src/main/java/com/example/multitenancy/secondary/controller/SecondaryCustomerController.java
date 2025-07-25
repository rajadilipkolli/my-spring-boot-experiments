package com.example.multitenancy.secondary.controller;

import com.example.multitenancy.secondary.entities.SecondaryCustomer;
import com.example.multitenancy.secondary.model.request.SecondaryCustomerRequest;
import com.example.multitenancy.secondary.services.SecondaryCustomerService;
import com.example.multitenancy.utils.AppConstants;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/customers/secondary")
public class SecondaryCustomerController {

    private static final Logger log = LoggerFactory.getLogger(SecondaryCustomerController.class);
    private final SecondaryCustomerService secondaryCustomerService;

    public SecondaryCustomerController(SecondaryCustomerService secondaryCustomerService) {
        this.secondaryCustomerService = secondaryCustomerService;
    }

    @GetMapping
    public List<SecondaryCustomer> getAllCustomers(@RequestHeader(AppConstants.X_TENANT_ID) String tenantId) {
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
            @RequestBody @Valid SecondaryCustomerRequest secondaryCustomerRequest,
            @RequestHeader(AppConstants.X_TENANT_ID) String tenantId) {
        log.info("creating customer by for tenant : {}", tenantId);
        return secondaryCustomerService.saveCustomer(secondaryCustomerRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SecondaryCustomer> updateCustomer(
            @PathVariable Long id,
            @RequestBody @Valid SecondaryCustomerRequest secondaryCustomerRequest,
            @RequestHeader(AppConstants.X_TENANT_ID) String tenantId) {
        log.info("updating customer for id {} in tenant : {}", id, tenantId);
        return secondaryCustomerService
                .findCustomerById(id)
                .map(customerObj -> {
                    SecondaryCustomer secondaryCustomer = new SecondaryCustomer();
                    secondaryCustomer.setId(id);
                    secondaryCustomer.setName(secondaryCustomerRequest.name());
                    return ResponseEntity.ok(secondaryCustomerService.saveCustomer(secondaryCustomer));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SecondaryCustomer> deleteCustomer(
            @PathVariable Long id, @RequestHeader(AppConstants.X_TENANT_ID) String tenantId) {
        log.info("deleting customer by id {} for tenant : {}", id, tenantId);
        return secondaryCustomerService
                .findCustomerById(id)
                .map(customer -> {
                    secondaryCustomerService.deleteCustomerById(id);
                    return ResponseEntity.ok(customer);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
