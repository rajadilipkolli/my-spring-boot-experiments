package com.example.graphql.controller;

import static org.mockito.BDDMockito.given;

import com.example.graphql.dtos.Customer;
import com.example.graphql.dtos.CustomerDTO;
import com.example.graphql.dtos.Orders;
import com.example.graphql.service.CustomerGraphQLService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@GraphQlTest(CustomerGraphQLController.class)
class CustomerGraphQLControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private CustomerGraphQLService customerGraphQLService;

    @Test
    void query_all_customers() {
        Customer customer = new Customer(1, "junit");
        given(customerGraphQLService.findAllCustomers()).willReturn(Flux.just(customer));
        given(customerGraphQLService.findAllOrdersByCustomers(List.of(customer)))
                .willReturn(Mono.just(Map.of(customer, List.of(new Orders(2, 1)))));
        this.graphQlTester
                .document("""
            query {
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
                .hasValue()
                .entityList(CustomerDTO.class)
                .hasSize(1);
    }
}
