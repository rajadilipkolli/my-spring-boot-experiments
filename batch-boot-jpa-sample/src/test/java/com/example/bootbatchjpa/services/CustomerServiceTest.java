package com.example.bootbatchjpa.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import com.example.bootbatchjpa.entities.Customer;
import com.example.bootbatchjpa.model.response.PagedResult;
import com.example.bootbatchjpa.repositories.CustomerRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void findAllCustomers() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Customer> customerPage = new PageImpl<>(List.of(getCustomer()));
        given(customerRepository.findAll(pageable)).willReturn(customerPage);

        // when
        PagedResult<Customer> pagedResult = customerService.findAllCustomers(0, 10, "id", "asc");

        // then
        assertThat(pagedResult).isNotNull();
        assertThat(pagedResult.data()).isNotEmpty().hasSize(1);
        assertThat(pagedResult.hasNext()).isFalse();
        assertThat(pagedResult.pageNumber()).isOne();
        assertThat(pagedResult.totalPages()).isOne();
        assertThat(pagedResult.isFirst()).isTrue();
        assertThat(pagedResult.isLast()).isTrue();
        assertThat(pagedResult.hasPrevious()).isFalse();
        assertThat(pagedResult.totalElements()).isOne();
    }

    @Test
    void findCustomerById() {
        // given
        given(customerRepository.findById(1L)).willReturn(Optional.of(getCustomer()));
        // when
        Optional<Customer> optionalCustomer = customerService.findCustomerById(1L);
        // then
        assertThat(optionalCustomer).isPresent();
        Customer customer = optionalCustomer.get();
        assertThat(customer.getId()).isOne();
        assertThat(customer.getName()).isEqualTo("junitName");
    }

    @Test
    void saveCustomer() {
        // given
        given(customerRepository.save(getCustomer())).willReturn(getCustomer());
        // when
        Customer persistedCustomer = customerService.saveCustomer(getCustomer());
        // then
        assertThat(persistedCustomer).isNotNull();
        assertThat(persistedCustomer.getId()).isOne();
        assertThat(persistedCustomer.getName()).isEqualTo("junitName");
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
        customer.setName("junitName");
        customer.setAddress("junitAddress");
        customer.setGender("male");
        return customer;
    }
}
