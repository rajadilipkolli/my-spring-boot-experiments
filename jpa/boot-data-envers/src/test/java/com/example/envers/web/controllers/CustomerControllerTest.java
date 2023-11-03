package com.example.envers.web.controllers;

import static com.example.envers.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.envers.entities.Customer;
import com.example.envers.exception.CustomerNotFoundException;
import com.example.envers.model.query.FindCustomersQuery;
import com.example.envers.model.request.CustomerRequest;
import com.example.envers.model.response.CustomerResponse;
import com.example.envers.model.response.PagedResult;
import com.example.envers.services.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CustomerController.class)
@ActiveProfiles(PROFILE_TEST)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<Customer> customerList;

    @BeforeEach
    void setUp() {
        this.customerList = new ArrayList<>();
        this.customerList.add(new Customer(1L, "text 1", "Junit Address", 0L));
        this.customerList.add(new Customer(2L, "text 2", "Junit Address", 0L));
        this.customerList.add(new Customer(3L, "text 3", "Junit Address", 0L));
    }

    @Test
    void shouldFetchAllCustomers() throws Exception {

        Page<Customer> page = new PageImpl<>(customerList);
        PagedResult<CustomerResponse> customerPagedResult = new PagedResult<>(page, getCustomerResponseList());
        FindCustomersQuery findCustomersQuery = new FindCustomersQuery(0, 10, "id", "asc");
        given(customerService.findAllCustomers(findCustomersQuery)).willReturn(customerPagedResult);

        this.mockMvc
                .perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(customerList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindCustomerById() throws Exception {
        Long customerId = 1L;
        CustomerResponse customer = new CustomerResponse(customerId, "text 1", "Junit Address");
        given(customerService.findCustomerById(customerId)).willReturn(Optional.of(customer));

        this.mockMvc
                .perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(customer.name())))
                .andExpect(jsonPath("$.address", is(customer.address())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingCustomer() throws Exception {
        Long customerId = 1L;
        given(customerService.findCustomerById(customerId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-data-envers.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Customer with Id '%d' not found".formatted(customerId)));
    }

    @Test
    void shouldCreateNewCustomer() throws Exception {

        CustomerResponse customer = new CustomerResponse(1L, "some text", "Junit Address");
        CustomerRequest customerRequest = new CustomerRequest("some text", "Junit Address");
        given(customerService.saveCustomer(any(CustomerRequest.class))).willReturn(customer);

        this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(customer.name())))
                .andExpect(jsonPath("$.address", is(customer.address())));
    }

    @Test
    void shouldReturn400WhenCreateNewCustomerWithoutName() throws Exception {
        CustomerRequest customerRequest = new CustomerRequest(null, null);

        this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/customers")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("name")))
                .andExpect(jsonPath("$.violations[0].message", is("Name cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        Long customerId = 1L;
        CustomerResponse customer = new CustomerResponse(customerId, "Updated text", "Junit Address");
        CustomerRequest customerRequest = new CustomerRequest("Updated text", "Junit Address");
        given(customerService.updateCustomer(eq(customerId), any(CustomerRequest.class)))
                .willReturn(customer);

        this.mockMvc
                .perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(customerId), Long.class))
                .andExpect(jsonPath("$.name", is(customer.name())))
                .andExpect(jsonPath("$.address", is(customer.address())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingCustomer() throws Exception {
        Long customerId = 1L;
        CustomerRequest customerRequest = new CustomerRequest("Updated text", "Junit Address");
        given(customerService.updateCustomer(eq(customerId), any(CustomerRequest.class)))
                .willThrow(new CustomerNotFoundException(customerId));

        this.mockMvc
                .perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-data-envers.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Customer with Id '%d' not found".formatted(customerId)));
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        Long customerId = 1L;
        CustomerResponse customer = new CustomerResponse(customerId, "Some text", "Junit Address");
        given(customerService.findCustomerById(customerId)).willReturn(Optional.of(customer));
        doNothing().when(customerService).deleteCustomerById(customerId);

        this.mockMvc
                .perform(delete("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(customer.name())))
                .andExpect(jsonPath("$.address", is(customer.address())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingCustomer() throws Exception {
        Long customerId = 1L;
        given(customerService.findCustomerById(customerId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/customers/{id}", customerId))
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-data-envers.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Customer with Id '%d' not found".formatted(customerId)));
    }

    List<CustomerResponse> getCustomerResponseList() {
        return customerList.stream()
                .map(customer -> new CustomerResponse(customer.getId(), customer.getName(), customer.getAddress()))
                .toList();
    }
}
