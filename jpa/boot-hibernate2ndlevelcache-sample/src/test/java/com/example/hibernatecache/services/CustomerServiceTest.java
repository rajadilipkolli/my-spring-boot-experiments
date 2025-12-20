package com.example.hibernatecache.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import com.example.hibernatecache.entities.Customer;
import com.example.hibernatecache.mapper.CustomerMapper;
import com.example.hibernatecache.model.response.CustomerResponse;
import com.example.hibernatecache.repositories.CustomerRepository;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void findCustomerById() {
        // given
        given(customerRepository.findById(1L)).willReturn(Optional.of(getCustomer()));
        given(customerMapper.toResponse(any(Customer.class))).willReturn(getCustomerResponse());
        // when
        Optional<CustomerResponse> optionalCustomer = customerService.findCustomerById(1L);
        // then
        assertThat(optionalCustomer).isPresent();
        CustomerResponse customer = optionalCustomer.get();
        assertThat(customer.customerId()).isOne();
        assertThat(customer.firstName()).isEqualTo("junitTest");
    }

    @Test
    void deleteCustomerById() {
        // given
        willDoNothing().given(customerRepository).deleteById(1L);
        // when
        customerService.deleteCustomerById(1L);
        // then
        verify(customerRepository, times(1)).deleteById(1L);
    }

    private Customer getCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("junitTest");
        return customer;
    }

    private CustomerResponse getCustomerResponse() {
        return new CustomerResponse(1L, "junitTest", "lastName", "email", "phone", Collections.emptyList());
    }
}
