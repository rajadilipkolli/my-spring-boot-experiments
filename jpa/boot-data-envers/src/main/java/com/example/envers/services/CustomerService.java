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
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionSort;
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
        List<CompletableFuture<RevisionResult>> revisionCFList = customerRepository
                .findRevisions(id)
                .get()
                .map(customerRevision -> CompletableFuture.supplyAsync(
                        () -> customerRevisionToRevisionDTOMapper.convert(customerRevision)))
                .toList();
        return revisionCFList.stream().map(CompletableFuture::join).toList();
    }

    public PagedResult<RevisionResult> findCustomerHistoryById(Long id, Pageable pageRequest) {
        if (customerRepository.findById(id).isEmpty()) {
            throw new CustomerNotFoundException(id);
        }

        RevisionSort sortDir = pageRequest.getSort().stream()
                .map(Sort.Order::getDirection)
                .findFirst()
                .map(direction -> {
                    if (Sort.Direction.ASC.name().equalsIgnoreCase(direction.name())) {
                        return RevisionSort.asc();
                    } else {
                        return RevisionSort.desc();
                    }
                })
                .orElse(RevisionSort.desc());

        Pageable pageable = PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), sortDir);
        Page<Revision<Integer, Customer>> customerRevisions = customerRepository.findRevisions(id, pageable);
        List<CompletableFuture<RevisionResult>> revisionCFResultList = customerRevisions.getContent().stream()
                .map(customerRevision -> CompletableFuture.supplyAsync(
                        () -> customerRevisionToRevisionDTOMapper.convert(customerRevision)))
                .toList();
        List<RevisionResult> revisionResultList =
                revisionCFResultList.stream().map(CompletableFuture::join).toList();
        return new PagedResult<>(customerRevisions, revisionResultList);
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
