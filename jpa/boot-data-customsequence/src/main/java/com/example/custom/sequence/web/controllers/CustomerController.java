package com.example.custom.sequence.web.controllers;

import com.example.custom.sequence.entities.Customer;
import com.example.custom.sequence.model.request.CustomerRequest;
import com.example.custom.sequence.model.request.ValidationGroups;
import com.example.custom.sequence.model.response.CustomerResponse;
import com.example.custom.sequence.model.response.PagedResult;
import com.example.custom.sequence.services.CustomerService;
import com.example.custom.sequence.utils.AppConstants;
import com.example.custom.sequence.web.api.CustomerAPI;
import jakarta.validation.groups.Default;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController implements CustomerAPI {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @Override
    public PagedResult<Customer> getAllCustomers(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        return customerService.findAllCustomers(pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable String id) {
        return customerService.findCustomerById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound()
                .build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public CustomerResponse createCustomer(
            @RequestBody @Validated(value = {Default.class, ValidationGroups.SkipGroupCheck.class})
                    CustomerRequest customerRequest) {
        return customerService.saveCustomer(customerRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable String id,
            @RequestBody @Validated(value = {Default.class, ValidationGroups.GroupCheck.class})
                    CustomerRequest customerRequest) {
        return customerService
                .updateCustomerById(id, customerRequest)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CustomerResponse> deleteCustomer(@PathVariable String id) {
        return customerService.deleteCustomerById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound()
                .build());
    }
}
