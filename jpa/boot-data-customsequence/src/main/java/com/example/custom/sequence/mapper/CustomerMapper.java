package com.example.custom.sequence.mapper;

import com.example.custom.sequence.entities.Customer;
import com.example.custom.sequence.model.response.CustomerResponse;
import org.springframework.stereotype.Service;

@Service
public class CustomerMapper {
    public CustomerResponse mapToResponse(Customer saved) {
        return new CustomerResponse(saved.getId(), saved.getText());
    }
}
