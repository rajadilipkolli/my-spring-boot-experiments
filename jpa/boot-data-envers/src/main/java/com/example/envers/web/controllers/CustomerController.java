package com.example.envers.web.controllers;

import com.example.envers.exception.CustomerNotFoundException;
import com.example.envers.model.query.FindCustomersQuery;
import com.example.envers.model.request.CustomerRequest;
import com.example.envers.model.response.CustomerResponse;
import com.example.envers.model.response.PagedResult;
import com.example.envers.model.response.RevisionResult;
import com.example.envers.services.CustomerService;
import com.example.envers.utils.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public PagedResult<CustomerResponse> getAllCustomers(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        FindCustomersQuery findCustomersQuery = new FindCustomersQuery(pageNo, pageSize, sortBy, sortDir);
        return customerService.findAllCustomers(findCustomersQuery);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        return customerService
                .findCustomerById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @GetMapping("/{id}/revisions")
    public ResponseEntity<List<RevisionResult>> findCustomerRevisionsById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.findCustomerRevisionsById(id));
    }

    @Operation(summary = "Get Customer Version History By Id")
    @GetMapping("/{id}/history")
    public PagedResult<RevisionResult> getCustomerHistoryById(@PathVariable("id") Long id, Pageable pageable) {
        return customerService.findCustomerHistoryById(id, pageable);
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@RequestBody @Validated CustomerRequest customerRequest) {
        CustomerResponse response = customerService.saveCustomer(customerRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/api/customers/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id, @RequestBody @Valid CustomerRequest customerRequest) {
        return ResponseEntity.ok(customerService.updateCustomer(id, customerRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CustomerResponse> deleteCustomer(@PathVariable Long id) {
        return customerService
                .findCustomerById(id)
                .map(customer -> {
                    customerService.deleteCustomerById(id);
                    return ResponseEntity.ok(customer);
                })
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }
}
