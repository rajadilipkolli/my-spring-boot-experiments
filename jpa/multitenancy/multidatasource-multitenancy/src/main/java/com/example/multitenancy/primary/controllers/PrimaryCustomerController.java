package com.example.multitenancy.primary.controllers;

import com.example.multitenancy.primary.entities.PrimaryCustomer;
import com.example.multitenancy.primary.model.request.PrimaryCustomerRequest;
import com.example.multitenancy.primary.services.PrimaryCustomerService;
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
@RequestMapping("/api/customers/primary")
public class PrimaryCustomerController {

    private static final Logger log = LoggerFactory.getLogger(PrimaryCustomerController.class);
    private final PrimaryCustomerService primaryCustomerService;

    public PrimaryCustomerController(PrimaryCustomerService primaryCustomerService) {
        this.primaryCustomerService = primaryCustomerService;
    }

    @GetMapping
    public List<PrimaryCustomer> getAllCustomers(@RequestHeader(AppConstants.X_TENANT_ID) String tenantId) {
        log.info("fetching all customer for tenant : {}", tenantId);
        return primaryCustomerService.findAllCustomers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrimaryCustomer> getCustomerById(
            @PathVariable Long id, @RequestHeader(AppConstants.X_TENANT_ID) String tenantId) {
        log.info("fetching customer by id {} for tenant : {}", id, tenantId);
        return primaryCustomerService
                .findCustomerById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrimaryCustomer createCustomer(
            @RequestBody @Valid PrimaryCustomerRequest primaryCustomerRequest,
            @RequestHeader(AppConstants.X_TENANT_ID) String tenantId) {
        log.info("creating customer by for tenant : {}", tenantId);
        return primaryCustomerService.saveCustomer(primaryCustomerRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PrimaryCustomer> updateCustomer(
            @PathVariable Long id,
            @RequestBody @Valid PrimaryCustomerRequest primaryCustomerRequest,
            @RequestHeader(AppConstants.X_TENANT_ID) String tenantId) {
        log.info("updating customer for id {} in tenant : {}", id, tenantId);
        return primaryCustomerService
                .findCustomerById(id)
                .map(customerObj -> {
                    PrimaryCustomer primaryCustomer = new PrimaryCustomer();
                    primaryCustomer.setText(primaryCustomerRequest.text());
                    primaryCustomer.setId(customerObj.getId());
                    return ResponseEntity.ok(primaryCustomerService.saveCustomer(primaryCustomer));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PrimaryCustomer> deleteCustomer(
            @PathVariable Long id, @RequestHeader(AppConstants.X_TENANT_ID) String tenantId) {
        log.info("deleting customer by id {} for tenant : {}", id, tenantId);
        return primaryCustomerService
                .findCustomerById(id)
                .map(customer -> {
                    primaryCustomerService.deleteCustomerById(id);
                    return ResponseEntity.ok(customer);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
