package com.example.envers.services;

import com.example.envers.entities.Customer;
import com.example.envers.mapper.CustomerRevisionToRevisionDTOMapper;
import com.example.envers.model.RevisionDTO;
import com.example.envers.repositories.CustomerRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerRevisionToRevisionDTOMapper customerRevisionToRevisionDTOMapper;

    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> findCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public List<RevisionDTO> findCustomerRevisionsById(Long id) {
        return customerRepository.findRevisions(id).getContent().stream()
                .collect(Collectors.mapping(customerRevisionToRevisionDTOMapper::convert, Collectors.toList()));
    }

    @Transactional
    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    @Transactional
    public void deleteCustomerById(Long id) {
        customerRepository.deleteById(id);
    }
}
