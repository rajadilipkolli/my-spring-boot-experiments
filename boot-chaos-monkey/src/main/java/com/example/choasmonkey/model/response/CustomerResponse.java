package com.example.choasmonkey.model.response;

import com.example.choasmonkey.entities.Customer;
import java.util.List;

public class CustomerResponse {

    private List<Customer> content;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
