package com.example.graphql.controller;

import com.example.graphql.dtos.Customer;
import com.example.graphql.dtos.CustomerConnection;
import com.example.graphql.dtos.CustomerEdge;
import com.example.graphql.dtos.Orders;
import com.example.graphql.dtos.PageInfo;
import com.example.graphql.service.CustomerGraphQLService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@Validated
public class CustomerGraphQLController {

    private final CustomerGraphQLService customerGraphQLService;

    public CustomerGraphQLController(CustomerGraphQLService customerGraphQLService) {
        this.customerGraphQLService = customerGraphQLService;
    }

    //    @SchemaMapping(typeName = "Query", field = "customers") or
    @QueryMapping
    public Mono<CustomerConnection> customers(
            @Argument(name = "first") Integer first,
            @Argument(name = "after") String after,
            @Argument(name = "last") Integer last,
            @Argument(name = "before") String before) {
        final int DEFAULT_PAGE_SIZE = 20;
        int limit = first != null ? first : (last != null ? last : DEFAULT_PAGE_SIZE);
        int offset = 0;
        if (after != null && !after.isBlank()) {
            try {
                var decoded = new String(Base64.getDecoder().decode(after));
                offset = Integer.parseInt(decoded);
            } catch (Exception e) {
                offset = 0;
            }
        }

        final int finalOffset = offset;
        final int finalLimit = limit;
        return this.customerGraphQLService
                .findAllCustomers(finalOffset, finalLimit)
                .collectList()
                .map(list -> {
                    boolean hasNext = list.size() > finalLimit;
                    var results = list.size() > finalLimit ? list.subList(0, finalLimit) : list;
                    List<CustomerEdge> edges = new ArrayList<>();
                    for (int i = 0; i < results.size(); i++) {
                        var cust = results.get(i);
                        int cursorIndex = finalOffset + i + 1; // cursor points to next offset
                        String cursor = Base64.getEncoder()
                                .encodeToString(String.valueOf(cursorIndex).getBytes());
                        edges.add(new CustomerEdge(cust, cursor));
                    }
                    String startCursor = edges.isEmpty() ? null : edges.get(0).getCursor();
                    String endCursor =
                            edges.isEmpty() ? null : edges.get(edges.size() - 1).getCursor();
                    var pageInfo = new PageInfo(false, hasNext, startCursor, endCursor);
                    return new CustomerConnection(edges, pageInfo);
                });
    }

    @QueryMapping
    Flux<Customer> customersByName(@Argument String name) {
        return this.customerGraphQLService.findByNameIgnoringCase(name);
    }

    //  @SchemaMapping(typeName = "Customer")
    //  Flux<Orders> orders(Customer customer) {
    //    return this.ordersRepository.findByCustomerId(customer.id());
    //  }

    // replacement for above code
    @BatchMapping(typeName = "Customer")
    public Mono<Map<Customer, List<Orders>>> orders(List<Customer> customers) {
        return this.customerGraphQLService.findAllOrdersByCustomers(customers);
    }

    @MutationMapping
    Mono<Customer> addCustomer(@Argument @NotBlank(message = "Name cant be blank") String name) {
        return this.customerGraphQLService.addCustomer(name);
    }

    @MutationMapping
    Mono<Orders> addOrderToCustomer(@Argument @Positive Integer id) {
        return this.customerGraphQLService.addOrderToCustomer(id);
    }
}
