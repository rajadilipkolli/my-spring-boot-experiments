package com.example.custom.sequence.services;

import com.example.custom.sequence.entities.Customer;
import com.example.custom.sequence.entities.Order;
import com.example.custom.sequence.model.response.CustomerDTO;
import com.example.custom.sequence.model.response.OrderDTO;
import com.example.custom.sequence.model.response.PagedResult;
import com.example.custom.sequence.repositories.OrderRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public PagedResult<OrderDTO> findAllOrders(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Order> ordersPage = orderRepository.findAll(pageable);

        List<OrderDTO> orderDTOList =
                ordersPage.getContent().stream().map(this::getOrderDTO).toList();

        return new PagedResult<>(
                orderDTOList,
                ordersPage.getTotalElements(),
                ordersPage.getNumber() + 1,
                ordersPage.getTotalPages(),
                ordersPage.isFirst(),
                ordersPage.isLast(),
                ordersPage.hasNext(),
                ordersPage.hasPrevious());
    }

    public Optional<OrderDTO> findOrderById(String id) {
        return convertToOrderDTO(orderRepository.findById(id));
    }

    public OrderDTO saveOrder(Order order) {
        return getOrderDTO(orderRepository.save(order));
    }

    public void deleteOrderById(String id) {
        orderRepository.deleteById(id);
    }

    private Optional<OrderDTO> convertToOrderDTO(Optional<Order> optionalOrder) {
        if (optionalOrder.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(getOrderDTO(optionalOrder.get()));
        }
    }

    private OrderDTO getOrderDTO(Order order) {
        Customer customer = order.getCustomer();
        return new OrderDTO(
                order.getId(),
                order.getText(),
                new CustomerDTO(customer.getId(), customer.getText()));
    }
}
