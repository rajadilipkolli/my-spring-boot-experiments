package com.example.graphql.controller;

import com.example.graphql.dtos.Customer;
import com.example.graphql.dtos.CustomerDTO;
import com.example.graphql.dtos.Orders;
import com.example.graphql.repository.CustomerRepository;
import com.example.graphql.repository.OrdersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.graphql.test.tester.GraphQlTester;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.mockito.BDDMockito.given;

@GraphQlTest(CustomerGraphQLController.class)
class CustomerGraphQLControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private OrdersRepository ordersRepository;

    @Test
    void test_query_all_customers() {
        given(customerRepository.findAll()).willReturn(Flux.just(new Customer(1,"junit")));
        given(ordersRepository.findByCustomerIdIn(List.of(1))).willReturn(Flux.just(new Orders(2,1)));
        this.graphQlTester
            .query("""
            {
              customers {
                id
                name
                orders {
                  id
                }
              }
            }
            """)
            .execute()
            .path("customers[*]")
            .pathExists()
            .valueIsNotEmpty()
            .entityList(CustomerDTO.class)
            .hasSize(1);
    }
}
