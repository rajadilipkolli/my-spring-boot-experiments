package com.example.custom.sequence.services;

import com.example.custom.sequence.entities.Customer;
import com.example.custom.sequence.mapper.CustomerMapper;
import com.example.custom.sequence.model.response.CustomerResponse;
import com.example.custom.sequence.model.response.PagedResult;
import com.example.custom.sequence.repositories.CustomerRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional(readOnly = true)
    public PagedResult<Customer> findAllCustomers(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<String> customerPage = customerRepository.findAllCustomerIds(pageable);
        List<Customer> customersList =
                customerRepository.findAllByIdWithOrders(customerPage.getContent());

        return new PagedResult<>(
                customersList,
                customerPage.getTotalElements(),
                customerPage.getNumber() + 1,
                customerPage.getTotalPages(),
                customerPage.isFirst(),
                customerPage.isLast(),
                customerPage.hasNext(),
                customerPage.hasPrevious());
    }

    public Optional<CustomerResponse> findCustomerById(String id) {
        return customerRepository.findById(id).map(customerMapper::mapToResponse);
    }

    public CustomerResponse saveCustomer(Customer customer) {
        Customer saved = customerRepository.save(customer);
        return customerMapper.mapToResponse(saved);
    }

    public void deleteCustomerById(String id) {
        customerRepository.deleteById(id);
    }
}
