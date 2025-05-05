package com.example.hibernatecache.services;

import com.example.hibernatecache.entities.Customer;
import com.example.hibernatecache.entities.Order;
import com.example.hibernatecache.exception.OrderNotFoundException;
import com.example.hibernatecache.mapper.OrderMapper;
import com.example.hibernatecache.model.query.FindOrdersQuery;
import com.example.hibernatecache.model.request.OrderRequest;
import com.example.hibernatecache.model.response.OrderResponse;
import com.example.hibernatecache.model.response.PagedResult;
import com.example.hibernatecache.repositories.CustomerRepository;
import com.example.hibernatecache.repositories.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CustomerRepository customerRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public OrderService(
            OrderRepository orderRepository, OrderMapper orderMapper, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.customerRepository = customerRepository;
    }

    public PagedResult<OrderResponse> findAllOrders(FindOrdersQuery findOrdersQuery) {

        // create Pageable instance
        Pageable pageable = createPageable(findOrdersQuery);

        Page<Order> ordersPage = orderRepository.findAll(pageable);

        List<OrderResponse> orderResponseList = orderMapper.mapToOrderResponseList(ordersPage.getContent());

        return new PagedResult<>(ordersPage, orderResponseList);
    }

    private Pageable createPageable(FindOrdersQuery findOrdersQuery) {
        int pageNo = Math.max(findOrdersQuery.pageNo() - 1, 0);
        Sort sort = Sort.by(
                findOrdersQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.Order.asc(findOrdersQuery.sortBy())
                        : Sort.Order.desc(findOrdersQuery.sortBy()));
        return PageRequest.of(pageNo, findOrdersQuery.pageSize(), sort);
    }

    public List<OrderResponse> findOrdersByCustomerId(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        return orderMapper.mapToOrderResponseList(orders);
    }

    public Optional<OrderResponse> findOrderById(Long id) {
        return orderRepository.findById(id).map(orderMapper::toResponse);
    }

    @Transactional
    public OrderResponse saveOrder(OrderRequest orderRequest) {
        Order order = orderMapper.toEntity(orderRequest);
        Order savedOrder = orderRepository.persist(order);
        return orderMapper.toResponse(savedOrder);
    }

    @Transactional
    public OrderResponse updateOrder(Long id, OrderRequest orderRequest) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

        // Update the order object with data from orderRequest
        orderMapper.updateOrderWithRequest(orderRequest, order);

        // Save the updated order object
        Order updatedOrder = orderRepository.merge(order);

        return orderMapper.toResponse(updatedOrder);
    }

    @Transactional
    public void deleteOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

        // Delete order
        orderRepository.deleteById(id);

        // Force cache eviction for the customer's orders collection
        entityManager
                .getEntityManagerFactory()
                .getCache()
                .evict(Customer.class, order.getCustomer().getId());
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }
}
