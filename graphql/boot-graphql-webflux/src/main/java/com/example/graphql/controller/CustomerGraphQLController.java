package com.example.graphql.controller;

import com.example.graphql.dtos.Customer;
import com.example.graphql.dtos.CustomerConnection;
import com.example.graphql.dtos.CustomerEdge;
import com.example.graphql.dtos.Orders;
import com.example.graphql.dtos.PageInfo;
import com.example.graphql.service.CustomerGraphQLService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.nio.charset.StandardCharsets;
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

    private static final int MAX_PAGE_SIZE = 100;

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
        if (first != null && last != null) {
            return Mono.error(new IllegalArgumentException("Cannot specify both first and last"));
        }
        if (after != null && before != null) {
            return Mono.error(new IllegalArgumentException("Cannot specify both after and before"));
        }
        if (before != null && last == null) {
            return Mono.error(new IllegalArgumentException("The 'before' argument requires 'last'"));
        }

        int limit = first != null ? first : (last != null ? last : 20);
        if (limit <= 0) {
            return Mono.error(new IllegalArgumentException("Page size must be greater than zero"));
        }
        if (limit > MAX_PAGE_SIZE) {
            return Mono.error(new IllegalArgumentException("Page size must not exceed " + MAX_PAGE_SIZE));
        }

        if (last != null) {
            if (before != null && !before.isBlank()) {
                int decodedBefore = decodeCursor(before);
                int offset = Math.max(0, decodedBefore - limit - 1);
                final int queryLimit = limit;
                final int queryOffset = offset;
                return this.customerGraphQLService
                        .findAllCustomers(queryOffset, queryLimit)
                        .collectList()
                        .zipWith(this.customerGraphQLService.countCustomers())
                        .map(tuple -> buildBackwardConnection(
                                tuple.getT1(), queryOffset, queryLimit, decodedBefore, tuple.getT2()));
            }
            final int queryLimit = limit;
            return this.customerGraphQLService.countCustomers().flatMap(total -> {
                int offset = Math.max(0, Math.toIntExact(total - queryLimit));
                final int queryOffset = offset;
                return this.customerGraphQLService
                        .findAllCustomers(queryOffset, queryLimit + 1)
                        .collectList()
                        .map(list ->
                                buildBackwardConnection(list, queryOffset, queryLimit, Math.toIntExact(total), total));
            });
        }

        int offset = 0;
        if (after != null && !after.isBlank()) {
            offset = decodeCursor(after);
        }

        final int queryLimit = limit;
        final int queryOffset = offset;
        return this.customerGraphQLService
                .findAllCustomers(queryOffset, queryLimit + 1)
                .collectList()
                .map(list -> buildForwardConnection(list, queryOffset, queryLimit));
    }

    private CustomerConnection buildForwardConnection(List<Customer> list, int offset, int limit) {
        boolean hasNext = list.size() > limit;
        var results = list.size() > limit ? list.subList(0, limit) : list;
        List<CustomerEdge> edges = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            var cust = results.get(i);
            int cursorIndex = offset + i + 1;
            String cursor = Base64.getEncoder()
                    .encodeToString(String.valueOf(cursorIndex).getBytes(StandardCharsets.UTF_8));
            edges.add(new CustomerEdge(cust, cursor));
        }
        String startCursor = edges.isEmpty() ? null : edges.get(0).getCursor();
        String endCursor = edges.isEmpty() ? null : edges.get(edges.size() - 1).getCursor();
        var pageInfo = new PageInfo(offset > 0, hasNext, startCursor, endCursor);
        return new CustomerConnection(edges, pageInfo);
    }

    private CustomerConnection buildBackwardConnection(
            List<Customer> list, int offset, int limit, int beforeOffset, long totalCount) {
        var results = list;
        List<CustomerEdge> edges = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            var cust = results.get(i);
            int cursorIndex = offset + i + 1;
            String cursor = Base64.getEncoder()
                    .encodeToString(String.valueOf(cursorIndex).getBytes(StandardCharsets.UTF_8));
            edges.add(new CustomerEdge(cust, cursor));
        }
        String startCursor = edges.isEmpty() ? null : edges.get(0).getCursor();
        String endCursor = edges.isEmpty() ? null : edges.get(edges.size() - 1).getCursor();
        boolean hasPrevious = offset > 0;
        boolean hasNext = beforeOffset < totalCount;
        var pageInfo = new PageInfo(hasPrevious, hasNext, startCursor, endCursor);
        return new CustomerConnection(edges, pageInfo);
    }

    private int decodeCursor(String cursor) {
        try {
            var decoded = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
            int value = Integer.parseInt(decoded);
            if (value <= 0) {
                throw new IllegalArgumentException("Invalid cursor: " + cursor);
            }
            return value;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor: " + cursor, e);
        }
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
