package com.example.custom.sequence.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import com.example.custom.sequence.entities.Customer;
import com.example.custom.sequence.mapper.CustomerMapper;
import com.example.custom.sequence.model.request.CustomerRequest;
import com.example.custom.sequence.model.request.OrderRequest;
import com.example.custom.sequence.model.response.CustomerResponse;
import com.example.custom.sequence.model.response.OrderResponseWithOutCustomer;
import com.example.custom.sequence.model.response.PagedResult;
import com.example.custom.sequence.repositories.CustomerRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void findAllCustomers() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        given(customerRepository.findAllCustomerIds(pageable)).willReturn(new PageImpl<>(List.of("CUS_1")));
        given(customerRepository.findAllByIdWithOrders(List.of("CUS_1"))).willReturn(List.of(getCustomer()));

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
        given(customerRepository.findById("CUS_1")).willReturn(Optional.of(getCustomer()));
        given(customerMapper.mapToResponse(getCustomer())).willReturn(getCustomerResponse());
        // when
        Optional<CustomerResponse> optionalCustomer = customerService.findCustomerById("CUS_1");
        // then
        assertThat(optionalCustomer).isPresent();
        CustomerResponse customer = optionalCustomer.get();
        assertThat(customer.id()).isEqualTo("CUS_1");
        assertThat(customer.text()).isEqualTo("junitTest");
    }

    @Test
    void saveCustomer() {
        // given
        given(customerMapper.mapToEntity(getCustomerRequest())).willReturn(getCustomer());
        given(customerRepository.persist(getCustomer())).willReturn(getCustomer());
        given(customerMapper.mapToResponse(getCustomer())).willReturn(getCustomerResponse());
        // when
        CustomerResponse persistedCustomer = customerService.saveCustomer(getCustomerRequest());
        // then
        assertThat(persistedCustomer).isNotNull();
        assertThat(persistedCustomer.id()).isEqualTo("CUS_1");
        assertThat(persistedCustomer.text()).isEqualTo("junitTest");
    }

    @Test
    void deleteCustomerById() {
        // given
        willDoNothing().given(customerRepository).deleteById("CUS_1");
        given(customerRepository.findById("CUS_1")).willReturn(Optional.of(getCustomer()));
        given(customerMapper.mapToResponse(getCustomer())).willReturn(getCustomerResponse());
        // when
        Optional<CustomerResponse> response = customerService.deleteCustomerById("CUS_1");
        // then
        assertThat(response).isPresent();
        assertThat(response.get().id()).isEqualTo("CUS_1");
        assertThat(response.get().text()).isEqualTo("junitTest");
        verify(customerRepository, times(1)).deleteById("CUS_1");
    }

    private Customer getCustomer() {
        Customer customer = new Customer();
        customer.setId("CUS_1");
        customer.setText("junitTest");
        return customer;
    }

    private CustomerRequest getCustomerRequest() {
        return new CustomerRequest("junitTest", List.of(new OrderRequest("ORD_1", "junitTest")));
    }

    private CustomerResponse getCustomerResponse() {
        return new CustomerResponse(
                "CUS_1", "junitTest", List.of(new OrderResponseWithOutCustomer("ORD_1", "junitTest")));
    }
}
