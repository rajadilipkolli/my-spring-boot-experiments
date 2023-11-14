package com.example.graphql.service;

import com.example.graphql.dtos.Customer;
import com.example.graphql.dtos.Orders;
import com.example.graphql.repository.CustomerRepository;
import com.example.graphql.repository.OrdersRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CustomerGraphQLServiceImpl implements CustomerGraphQLService {

    private final CustomerRepository customerRepository;

    private final OrdersRepository ordersRepository;

    public CustomerGraphQLServiceImpl(CustomerRepository customerRepository, OrdersRepository ordersRepository) {
        this.customerRepository = customerRepository;
        this.ordersRepository = ordersRepository;
    }

    @Override
    public Flux<Customer> findAllCustomers() {
        return this.customerRepository.findAll();
    }

    @Override
    public Flux<Customer> findByNameIgnoringCase(String name) {
        return this.customerRepository.findByNameIgnoringCase(name);
    }

    @Override
    public Mono<Map<Customer, List<Orders>>> findAllOrdersByCustomers(List<Customer> customers) {
        var keys = customers.stream().map(Customer::id).toList();
        return this.ordersRepository
                .findByCustomerIdIn(keys)
                .collectMultimap(Orders::customerId)
                .map(customerOrderMap -> {
                    var result = new HashMap<Customer, List<Orders>>();
                    customerOrderMap.keySet().forEach(customerId -> {
                        var customer = customers.stream()
                                .filter(cust -> cust.id().equals(customerId))
                                .findAny()
                                .orElseThrow();
                        result.put(customer, new ArrayList<>(customerOrderMap.get(customerId)));
                    });
                    return result;
                });
    }

    @Override
    public Mono<Customer> addCustomer(String name) {
        return this.customerRepository.save(new Customer(null, name));
    }

    @Override
    public Mono<Orders> addOrderToCustomer(Integer id) {
        return this.ordersRepository.save(new Orders(null, id));
    }
}
