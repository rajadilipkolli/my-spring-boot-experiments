package com.example.bootbatchjpa.web.controllers;

import static com.example.bootbatchjpa.utils.AppConstants.PROFILE_TEST;
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

import com.example.bootbatchjpa.entities.Customer;
import com.example.bootbatchjpa.model.response.PagedResult;
import com.example.bootbatchjpa.services.CustomerService;
import java.util.List;
import java.util.Optional;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = CustomerController.class)
@ActiveProfiles(PROFILE_TEST)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Autowired
    private JsonMapper jsonMapper;

    private List<Customer> customerList;

    @BeforeEach
    void setUp() {
        this.customerList = Instancio.ofList(Customer.class).size(3).create();
    }

    @Test
    void shouldFetchAllCustomers() throws Exception {
        Page<Customer> page = new PageImpl<>(customerList);
        PagedResult<Customer> customerPagedResult = new PagedResult<>(page);
        given(customerService.findAllCustomers(0, 10, "id", "asc")).willReturn(customerPagedResult);

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
        Customer customer = Instancio.create(Customer.class);
        customer.setId(customerId);
        given(customerService.findCustomerById(customerId)).willReturn(Optional.of(customer));

        this.mockMvc
                .perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(customer.getName())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingCustomer() throws Exception {
        Long customerId = 1L;
        given(customerService.findCustomerById(customerId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/api/customers/{id}", customerId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewCustomer() throws Exception {
        given(customerService.saveCustomer(any(Customer.class))).willAnswer((invocation) -> invocation.getArgument(0));

        Customer customer = Instancio.create(Customer.class);
        this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(customer.getName())));
    }

    @Test
    void shouldReturn400WhenCreateNewCustomerWithoutText() throws Exception {
        Customer customer = new Customer(null, null, null, null);

        this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(customer)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://boot-jpa.com/errors/validation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/customers")))
                .andExpect(jsonPath("$.properties.violations", hasSize(1)))
                .andExpect(jsonPath("$.properties.violations[0].field", is("name")))
                .andExpect(jsonPath("$.properties.violations[0].message", is("Name cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        Long customerId = 1L;
        Customer customer = Instancio.create(Customer.class);
        customer.setId(customerId);
        customer.setName("Updated Name");
        given(customerService.findCustomerById(customerId)).willReturn(Optional.of(customer));
        given(customerService.saveCustomer(any(Customer.class))).willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(put("/api/customers/{id}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingCustomer() throws Exception {
        Long customerId = 1L;
        given(customerService.findCustomerById(customerId)).willReturn(Optional.empty());
        Customer customer = Instancio.create(Customer.class);

        this.mockMvc
                .perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(customer)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        Long customerId = 1L;
        Customer customer = Instancio.create(Customer.class);
        customer.setId(customerId);
        given(customerService.findCustomerById(customerId)).willReturn(Optional.of(customer));
        doNothing().when(customerService).deleteCustomerById(customer.getId());

        this.mockMvc
                .perform(delete("/api/customers/{id}", customer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(customer.getName())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingCustomer() throws Exception {
        Long customerId = 1L;
        given(customerService.findCustomerById(customerId)).willReturn(Optional.empty());

        this.mockMvc.perform(delete("/api/customers/{id}", customerId)).andExpect(status().isNotFound());
    }
}
