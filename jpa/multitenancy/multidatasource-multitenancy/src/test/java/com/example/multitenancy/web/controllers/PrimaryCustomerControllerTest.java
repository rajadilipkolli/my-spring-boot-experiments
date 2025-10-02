package com.example.multitenancy.web.controllers;

import static com.example.multitenancy.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
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

import com.example.multitenancy.config.multitenant.TenantIdentifierResolver;
import com.example.multitenancy.primary.controllers.PrimaryCustomerController;
import com.example.multitenancy.primary.entities.PrimaryCustomer;
import com.example.multitenancy.primary.model.request.PrimaryCustomerRequest;
import com.example.multitenancy.primary.services.PrimaryCustomerService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = PrimaryCustomerController.class)
@ActiveProfiles(PROFILE_TEST)
class PrimaryCustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PrimaryCustomerService primaryCustomerService;

    @MockitoBean
    private TenantIdentifierResolver tenantIdentifierResolver;

    private List<PrimaryCustomer> primaryCustomerList;

    @BeforeEach
    void setUp() {
        this.primaryCustomerList = new ArrayList<>();
        this.primaryCustomerList.add(new PrimaryCustomer()
                .setId(1L)
                .setText("text 1")
                .setVersion((short) 0)
                .setTenant("dbsystc"));
        this.primaryCustomerList.add(new PrimaryCustomer()
                .setId(2L)
                .setText("text 2")
                .setVersion((short) 0)
                .setTenant("dbsystc"));
        this.primaryCustomerList.add(new PrimaryCustomer()
                .setId(3L)
                .setText("text 3")
                .setVersion((short) 0)
                .setTenant("dbsystc"));
    }

    @Test
    void shouldFetchAllCustomers() throws Exception {
        given(primaryCustomerService.findAllCustomers()).willReturn(this.primaryCustomerList);

        this.mockMvc
                .perform(get("/api/customers/primary").header("X-tenantId", "primary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(primaryCustomerList.size())));
    }

    @Test
    void shouldFindCustomerById() throws Exception {
        Long customerId = 1L;
        PrimaryCustomer primaryCustomer = new PrimaryCustomer()
                .setId(customerId)
                .setText("text 1")
                .setVersion((short) 0)
                .setTenant("dbsystc");
        given(primaryCustomerService.findCustomerById(customerId)).willReturn(Optional.of(primaryCustomer));

        this.mockMvc
                .perform(get("/api/customers/primary/{id}", customerId).header("X-tenantId", "primary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(primaryCustomer.getText())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingCustomer() throws Exception {
        Long customerId = 1L;
        given(primaryCustomerService.findCustomerById(customerId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/customers/primary/{id}", customerId).header("X-tenantId", "primary"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewCustomer() throws Exception {

        PrimaryCustomer primaryCustomer = new PrimaryCustomer()
                .setId(1L)
                .setText("text 1")
                .setVersion((short) 0)
                .setTenant("dbsystc");

        given(primaryCustomerService.saveCustomer(any(PrimaryCustomerRequest.class)))
                .willReturn(primaryCustomer);

        PrimaryCustomerRequest request = new PrimaryCustomerRequest("some text");

        this.mockMvc
                .perform(post("/api/customers/primary")
                        .header("X-tenantId", "primary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(primaryCustomer.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewCustomerWithoutText() throws Exception {
        PrimaryCustomer primaryCustomer = new PrimaryCustomer().setTenant("dbsystc");

        this.mockMvc
                .perform(post("/api/customers/primary")
                        .header("X-tenantId", "primary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(primaryCustomer)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://multitenancy.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/customers/primary")))
                .andExpect(jsonPath("$.properties.violations", hasSize(1)))
                .andExpect(jsonPath("$.properties.violations[0].object", is("primaryCustomerRequest")))
                .andExpect(jsonPath("$.properties.violations[0].field", is("text")))
                .andExpect(jsonPath("$.properties.violations[0].rejectedValue", is(nullValue())))
                .andExpect(jsonPath("$.properties.violations[0].message", is("Text cannot be blank")))
                .andReturn();
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        Long customerId = 1L;
        PrimaryCustomer primaryCustomer = new PrimaryCustomer()
                .setId(customerId)
                .setText("text 1")
                .setVersion((short) 0)
                .setTenant("dbsystc");
        given(primaryCustomerService.findCustomerById(customerId)).willReturn(Optional.of(primaryCustomer));
        given(primaryCustomerService.saveCustomer(any(PrimaryCustomer.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(put("/api/customers/primary/{id}", primaryCustomer.getId())
                        .header("X-tenantId", "primary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(primaryCustomer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(primaryCustomer.getText())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingCustomer() throws Exception {
        Long customerId = 1L;
        given(primaryCustomerService.findCustomerById(customerId)).willReturn(Optional.empty());
        PrimaryCustomer primaryCustomer = new PrimaryCustomer()
                .setId(customerId)
                .setText("text 1")
                .setVersion((short) 0)
                .setTenant("dbsystc");

        this.mockMvc
                .perform(put("/api/customers/primary/{id}", customerId)
                        .header("X-tenantId", "primary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(primaryCustomer)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        Long customerId = 1L;
        PrimaryCustomer primaryCustomer = new PrimaryCustomer()
                .setId(customerId)
                .setText("text 1")
                .setVersion((short) 0)
                .setTenant("dbsystc");
        given(primaryCustomerService.findCustomerById(customerId)).willReturn(Optional.of(primaryCustomer));
        doNothing().when(primaryCustomerService).deleteCustomerById(primaryCustomer.getId());

        this.mockMvc
                .perform(delete("/api/customers/primary/{id}", primaryCustomer.getId())
                        .header("X-tenantId", "primary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(primaryCustomer.getText())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingCustomer() throws Exception {
        Long customerId = 1L;
        given(primaryCustomerService.findCustomerById(customerId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/customers/primary/{id}", customerId).header("X-tenantId", "primary"))
                .andExpect(status().isNotFound());
    }
}
