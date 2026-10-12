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
import org.springframework.http.HttpHeaders;
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

    /** Creates named customer fixtures with empty order lists for controller tests. */
    @BeforeEach
    void setUp() {
        this.customerList = new ArrayList<>();
        this.customerList.add(new Customer().setId("CUS_1").setName("text 1").setOrders(new ArrayList<>()));
        this.customerList.add(new Customer().setId("CUS_2").setName("text 2").setOrders(new ArrayList<>()));
        this.customerList.add(new Customer().setId("CUS_3").setName("text 3").setOrders(new ArrayList<>()));
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

    /** Verifies that fetching an existing customer exposes the customerName response field. */
    @Test
    void shouldFindCustomerById() throws Exception {
        String customerId = "CUS_1";
        CustomerResponse customer = new CustomerResponse(customerId, "text 1", List.of());
        given(customerService.findCustomerById(customerId)).willReturn(Optional.of(customer));

        this.mockMvc
                .perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName", is(customer.customerName())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingCustomer() throws Exception {
        String customerId = "CUS_1";
        given(customerService.findCustomerById(customerId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/api/customers/{id}", customerId)).andExpect(status().isNotFound());
    }

    /** Verifies that customer creation returns HTTP 201 with an identifier and the requested name. */
    @Test
    void shouldCreateNewCustomer() throws Exception {

        CustomerRequest customerRequest = new CustomerRequest("some text", new ArrayList<>());

        given(customerService.saveCustomer(any(CustomerRequest.class)))
                .willReturn(new CustomerResponse("CUS_1", "some text", List.of()));

        this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.customerName", is(customerRequest.customerName())));
    }

    /** Verifies that a null customer name returns HTTP 400 with the customerName violation. */
    @Test
    void shouldReturn400WhenCreateNewCustomerWithInvalidData() throws Exception {
        CustomerRequest customerRequest = new CustomerRequest(null, List.of(new OrderRequest("ORD_1", null)));

        this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://custom-sequence.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/customers")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("customerName")))
                .andExpect(jsonPath("$.violations[0].message", is("Customer name cannot be blank")))
                .andReturn();
    }

    /** Verifies that an empty customer name returns HTTP 400 with a field-specific validation error. */
    @Test
    void shouldReturn400WhenCreateNewCustomerWithEmptyCustomerName() throws Exception {
        CustomerRequest customerRequest = new CustomerRequest("", new ArrayList<>());
        this.mockMvc
                .perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://custom-sequence.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/customers")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("customerName")))
                .andExpect(jsonPath("$.violations[0].message", is("Customer name cannot be blank")));
    }

    /** Verifies that updating a customer returns its new name and order details. */
    @Test
    void shouldUpdateCustomer() throws Exception {
        String customerId = "CUS_1";
        CustomerResponse customerResponse = new CustomerResponse(
                customerId, "Updated Name", List.of(new OrderResponseWithOutCustomer("ORD_1", "New Order")));
        CustomerRequest customerRequest =
                new CustomerRequest("Updated Name", List.of(new OrderRequest("ORD_1", customerId)));
        given(customerService.updateCustomerById(customerId, customerRequest))
                .willReturn(Optional.of(customerResponse));

        this.mockMvc
                .perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName", is(customerResponse.customerName())))
                .andExpect(jsonPath("$.orderResponses", hasSize(1)))
                .andExpect(jsonPath("$.orderResponses[0].id", is("ORD_1")))
                .andExpect(jsonPath("$.orderResponses[0].orderDescription", is("New Order")));
    }

    /** Verifies that updating with an empty customer name returns the customerName validation error. */
    @Test
    void shouldReturn400WhenUpdateCustomerWithEmptyCustomerName() throws Exception {
        String customerId = "CUS_1";
        CustomerRequest customerRequest = new CustomerRequest("", List.of(new OrderRequest("ORD_1", customerId)));

        this.mockMvc
                .perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://custom-sequence.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/customers/CUS_1")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("customerName")))
                .andExpect(jsonPath("$.violations[0].message", is("Customer name cannot be blank")));
        ;
    }

    /** Verifies that updating a missing customer returns HTTP 404. */
    @Test
    void shouldReturn404WhenUpdatingNonExistingCustomer() throws Exception {
        String customerId = "CUS_1";
        given(customerService.findCustomerById(customerId)).willReturn(Optional.empty());
        CustomerRequest customerRequest =
                new CustomerRequest("Dummy Name", List.of(new OrderRequest("ORD_1", customerId)));

        this.mockMvc
                .perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isNotFound());
    }

    /** Verifies that deleting a customer returns its name in the response. */
    @Test
    void shouldDeleteCustomer() throws Exception {
        String customerId = "CUS_1";
        CustomerResponse customerResponse = new CustomerResponse(customerId, "Some text", List.of());
        given(customerService.deleteCustomerById(customerId)).willReturn(Optional.of(customerResponse));

        this.mockMvc
                .perform(delete("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName", is(customerResponse.customerName())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingCustomer() throws Exception {
        String customerId = "CUS_1";
        given(customerService.deleteCustomerById(customerId)).willReturn(Optional.empty());

        this.mockMvc.perform(delete("/api/customers/{id}", customerId)).andExpect(status().isNotFound());
    }
}
