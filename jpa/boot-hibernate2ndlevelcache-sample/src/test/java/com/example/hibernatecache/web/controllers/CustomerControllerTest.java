package com.example.hibernatecache.web.controllers;

import static com.example.hibernatecache.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.hibernatecache.model.request.CustomerRequest;
import com.example.hibernatecache.model.response.CustomerResponse;
import com.example.hibernatecache.model.response.PagedResult;
import com.example.hibernatecache.services.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

    @Test
    void shouldFetchAllCustomers() throws Exception {
        List<CustomerResponse> customerMappedList = getCustomerResponses();

        Page<CustomerResponse> page = new PageImpl<>(customerMappedList);
        PagedResult<CustomerResponse> customerPagedResult = new PagedResult<>(page, customerMappedList);
        given(customerService.findAllCustomers(0, 10, "id", "asc")).willReturn(customerPagedResult);

        this.mockMvc
                .perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(customerMappedList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    private static List<CustomerResponse> getCustomerResponses() {
        List<CustomerResponse> customerMappedList = new ArrayList<>();
        customerMappedList.add(
                new CustomerResponse(1L, "firstName 1", "lastName 1", "email1@junit.com", "9876543211", null));
        customerMappedList.add(
                new CustomerResponse(2L, "firstName 2", "lastName 2", "email2@junit.com", "9876543212", null));
        customerMappedList.add(
                new CustomerResponse(3L, "firstName 3", "lastName 3", "email3@junit.com", "9876543213", null));
        return customerMappedList;
    }

    @Test
    void shouldFindCustomerById() throws Exception {
        Long customerId = 1L;
        CustomerResponse customer =
                (new CustomerResponse(3L, "firstName 3", "lastName 3", "email3@junit.com", "9876543213", null));
        given(customerService.findCustomerById(customerId)).willReturn(Optional.of(customer));

        this.mockMvc
                .perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(customer.firstName())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingCustomer() throws Exception {
        Long customerId = 1L;
        given(customerService.findCustomerById(customerId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/api/customers/{id}", customerId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewCustomer() throws Exception {
        CustomerResponse customer =
                new CustomerResponse(3L, "firstName 3", "lastName 3", "email3@junit.com", "9876543213", null);
        CustomerRequest customerRequest =
                new CustomerRequest("firstName 3", "lastName 3", "email3@junit.com", "9876543213");
        given(customerService.saveCustomer(any(CustomerRequest.class))).willReturn(customer);
        this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId", notNullValue()))
                .andExpect(jsonPath("$.firstName", is(customer.firstName())));
    }

    @Test
    void shouldReturn400WhenCreateNewCustomerWithoutFirstName() throws Exception {
        CustomerRequest customer = new CustomerRequest(null, null, null, "9876543213");

        this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/customers")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("firstName")))
                .andExpect(jsonPath("$.violations[0].message", is("FirstName cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingCustomer() throws Exception {
        Long customerId = 1L;
        given(customerService.findCustomerById(customerId)).willReturn(Optional.empty());
        CustomerRequest customerRequest =
                new CustomerRequest("firstName 3", "lastName 3", "email3@junit.com", "9876543213");

        this.mockMvc
                .perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        Long customerId = 1L;
        CustomerResponse customer =
                new CustomerResponse(customerId, "firstName 3", "lastName 3", "email3@junit.com", "9876543213", null);
        given(customerService.findCustomerById(customerId)).willReturn(Optional.of(customer));
        doNothing().when(customerService).deleteCustomerById(customerId);

        this.mockMvc
                .perform(delete("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(customer.firstName())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingCustomer() throws Exception {
        Long customerId = 1L;
        given(customerService.findCustomerById(customerId)).willReturn(Optional.empty());

        this.mockMvc.perform(delete("/api/customers/{id}", customerId)).andExpect(status().isNotFound());
    }
}
