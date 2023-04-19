package com.example.custom.sequence.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import com.example.custom.sequence.entities.Customer;
import com.example.custom.sequence.model.response.PagedResult;
import com.example.custom.sequence.repositories.CustomerRepository;

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

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock private CustomerRepository customerRepository;

    @InjectMocks private CustomerService customerService;

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
        assertThat(pagedResult.pageNumber()).isEqualTo(1);
        assertThat(pagedResult.totalPages()).isEqualTo(1);
        assertThat(pagedResult.isFirst()).isTrue();
        assertThat(pagedResult.isLast()).isTrue();
        assertThat(pagedResult.hasPrevious()).isFalse();
        assertThat(pagedResult.totalElements()).isEqualTo(1);
    }

    @Test
    void findCustomerById() {
        // given
        given(customerRepository.findById("CUS_1")).willReturn(Optional.of(getCustomer()));
        // when
        Optional<Customer> optionalCustomer = customerService.findCustomerById("CUS_1");
        // then
        assertThat(optionalCustomer).isPresent();
        Customer customer = optionalCustomer.get();
        assertThat(customer.getId()).isEqualTo("CUS_1");
        assertThat(customer.getText()).isEqualTo("junitTest");
    }

    @Test
    void saveCustomer() {
        // given
        given(customerRepository.save(getCustomer())).willReturn(getCustomer());
        // when
        Customer persistedCustomer = customerService.saveCustomer(getCustomer());
        // then
        assertThat(persistedCustomer).isNotNull();
        assertThat(persistedCustomer.getId()).isEqualTo("CUS_1");
        assertThat(persistedCustomer.getText()).isEqualTo("junitTest");
    }

    @Test
    void deleteCustomerById() {
        // given
        willDoNothing().given(customerRepository).deleteById("CUS_1");
        // when
        customerService.deleteCustomerById("CUS_1");
        // then
        verify(customerRepository, times(1)).deleteById("CUS_1");
    }

    private Customer getCustomer() {
        Customer customer = new Customer();
        customer.setId("CUS_1");
        customer.setText("junitTest");
        return customer;
    }
}
