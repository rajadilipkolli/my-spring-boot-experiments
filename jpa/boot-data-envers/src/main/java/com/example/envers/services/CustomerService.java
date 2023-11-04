package com.example.envers.services;

import com.example.envers.entities.Customer;
import com.example.envers.exception.CustomerNotFoundException;
import com.example.envers.mapper.CustomerMapper;
import com.example.envers.mapper.CustomerRevisionToRevisionResultMapper;
import com.example.envers.model.query.FindCustomersQuery;
import com.example.envers.model.request.CustomerRequest;
import com.example.envers.model.response.CustomerResponse;
import com.example.envers.model.response.PagedResult;
import com.example.envers.model.response.RevisionResult;
import com.example.envers.repositories.CustomerRepository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerRevisionToRevisionResultMapper customerRevisionToRevisionDTOMapper;
    private final CustomerMapper customerMapper;

    public PagedResult<CustomerResponse> findAllCustomers(FindCustomersQuery findCustomersQuery) {

        // create Pageable instance
        Pageable pageable = createPageable(findCustomersQuery);

        Page<Customer> customersPage = customerRepository.findAll(pageable);

        List<CustomerResponse> customerResponseList = customerMapper.toResponseList(customersPage.getContent());

        return new PagedResult<>(customersPage, customerResponseList);
    }

    private Pageable createPageable(FindCustomersQuery findCustomersQuery) {
        int pageNo = Math.max(findCustomersQuery.pageNo() - 1, 0);
        Sort sort = Sort.by(
                findCustomersQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.Order.asc(findCustomersQuery.sortBy())
                        : Sort.Order.desc(findCustomersQuery.sortBy()));
        return PageRequest.of(pageNo, findCustomersQuery.pageSize(), sort);
    }

    public Optional<CustomerResponse> findCustomerById(Long id) {
        return customerRepository.findById(id).map(customerMapper::toResponse);
    }

    public List<RevisionResult> findCustomerRevisionsById(Long id) {
        List<CompletableFuture<RevisionResult>> revisionDtoCF = customerRepository
                .findRevisions(id)
                .get()
                .map(customerRevision -> CompletableFuture.supplyAsync(
                        () -> customerRevisionToRevisionDTOMapper.convert(customerRevision)))
                .toList();
        return revisionDtoCF.stream().map(CompletableFuture::join).toList();
    }

    @Transactional
    public CustomerResponse saveCustomer(CustomerRequest customerRequest) {
        Customer customer = customerMapper.toEntity(customerRequest);
        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toResponse(savedCustomer);
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest customerRequest) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));

        // Update the customer object with data from customerRequest
        customerMapper.mapCustomerWithRequest(customer, customerRequest);

        // Save the updated customer object
        Customer updatedCustomer = customerRepository.save(customer);

        return customerMapper.toResponse(updatedCustomer);
    }

    @Transactional
    public void deleteCustomerById(Long id) {
        customerRepository.deleteById(id);
    }
}
