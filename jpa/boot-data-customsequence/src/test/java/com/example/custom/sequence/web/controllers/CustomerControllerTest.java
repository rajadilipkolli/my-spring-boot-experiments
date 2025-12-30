package com.example.custom.sequence.web.controllers;

import static com.example.custom.sequence.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.custom.sequence.entities.Customer;
import com.example.custom.sequence.model.request.CustomerRequest;
import com.example.custom.sequence.model.request.OrderRequest;
import com.example.custom.sequence.model.response.CustomerResponse;
import com.example.custom.sequence.model.response.OrderResponseWithOutCustomer;
import com.example.custom.sequence.model.response.PagedResult;
import com.example.custom.sequence.services.CustomerService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = CustomerController.class)
@ActiveProfiles(PROFILE_TEST)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<Customer> customerList;

    @BeforeEach
    void setUp() {
        this.customerList = new ArrayList<>();
        this.customerList.add(new Customer().setId("CUS_1").setText("text 1").setOrders(new ArrayList<>()));
        this.customerList.add(new Customer().setId("CUS_2").setText("text 2").setOrders(new ArrayList<>()));
        this.customerList.add(new Customer().setId("CUS_3").setText("text 3").setOrders(new ArrayList<>()));
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
        String customerId = "CUS_1";
        CustomerResponse customer = new CustomerResponse(customerId, "text 1", List.of());
        given(customerService.findCustomerById(customerId)).willReturn(Optional.of(customer));

        this.mockMvc
                .perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(customer.text())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingCustomer() throws Exception {
        String customerId = "CUS_1";
        given(customerService.findCustomerById(customerId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/api/customers/{id}", customerId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewCustomer() throws Exception {

        CustomerRequest customerRequest = new CustomerRequest("some text", new ArrayList<>());

        given(customerService.saveCustomer(any(CustomerRequest.class)))
                .willReturn(new CustomerResponse("CUS_1", "some text", List.of()));

        this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(customerRequest.text())));
    }

    @Test
    void shouldReturn400WhenCreateNewCustomerWithInvalidData() throws Exception {
        CustomerRequest customerRequest = new CustomerRequest(null, List.of(new OrderRequest("ORD_1", null)));

        this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("https://custom-sequence.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/customers")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldReturn400WhenCreateNewCustomerWithEmptyText() throws Exception {
        CustomerRequest customerRequest = new CustomerRequest("", new ArrayList<>());
        this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("https://custom-sequence.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/customers")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")));
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        String customerId = "CUS_1";
        CustomerResponse customerResponse = new CustomerResponse(
                customerId, "Updated text", List.of(new OrderResponseWithOutCustomer("ORD_1", "New Order")));
        CustomerRequest customerRequest =
                new CustomerRequest("Updated text", List.of(new OrderRequest("ORD_1", customerId)));
        given(customerService.updateCustomerById(customerId, customerRequest))
                .willReturn(Optional.of(customerResponse));

        this.mockMvc
                .perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(customerResponse.text())))
                .andExpect(jsonPath("$.orderResponses", hasSize(1)))
                .andExpect(jsonPath("$.orderResponses[0].id", is("ORD_1")))
                .andExpect(jsonPath("$.orderResponses[0].orderDescription", is("New Order")));
    }

    @Test
    void shouldReturn400WhenUpdateCustomerWithEmpty() throws Exception {
        String customerId = "CUS_1";
        CustomerRequest customerRequest = new CustomerRequest("Updated text", List.of(new OrderRequest("ORD_1", null)));

        this.mockMvc
                .perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("https://custom-sequence.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/customers/CUS_1")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("orders[0].customerId")))
                .andExpect(jsonPath("$.violations[0].message", is("CustomerId cannot be blank")));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingCustomer() throws Exception {
        String customerId = "CUS_1";
        given(customerService.findCustomerById(customerId)).willReturn(Optional.empty());
        Customer customer = new Customer().setId(customerId).setText("Updated text");

        this.mockMvc
                .perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        String customerId = "CUS_1";
        CustomerResponse customerResponse = new CustomerResponse(customerId, "Some text", List.of());
        given(customerService.deleteCustomerById(customerId)).willReturn(Optional.of(customerResponse));

        this.mockMvc
                .perform(delete("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(customerResponse.text())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingCustomer() throws Exception {
        String customerId = "CUS_1";
        given(customerService.deleteCustomerById(customerId)).willReturn(Optional.empty());

        this.mockMvc.perform(delete("/api/customers/{id}", customerId)).andExpect(status().isNotFound());
    }
}
