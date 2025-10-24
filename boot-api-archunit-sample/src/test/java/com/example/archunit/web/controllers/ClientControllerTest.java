package com.example.archunit.web.controllers;

import static com.example.archunit.utils.AppConstants.PROFILE_TEST;
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

import com.example.archunit.entities.Client;
import com.example.archunit.exception.ClientNotFoundException;
import com.example.archunit.model.query.FindClientsQuery;
import com.example.archunit.model.request.ClientRequest;
import com.example.archunit.model.response.ClientResponse;
import com.example.archunit.model.response.PagedResult;
import com.example.archunit.services.ClientService;
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

@WebMvcTest(controllers = ClientController.class)
@ActiveProfiles(PROFILE_TEST)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService clientService;

    @Autowired
    private JsonMapper jsonMapper;

    private List<Client> clientList;

    @BeforeEach
    void setUp() {
        this.clientList = new ArrayList<>();
        this.clientList.add(new Client(1L, "text 1"));
        this.clientList.add(new Client(2L, "text 2"));
        this.clientList.add(new Client(3L, "text 3"));
    }

    @Test
    void shouldFetchAllClients() throws Exception {

        Page<Client> page = new PageImpl<>(clientList);
        PagedResult<ClientResponse> clientPagedResult = new PagedResult<>(page, getClientResponseList());
        FindClientsQuery findClientsQuery = new FindClientsQuery(0, 10, "id", "asc");
        given(clientService.findAllClients(findClientsQuery)).willReturn(clientPagedResult);

        this.mockMvc
                .perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(clientList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindClientById() throws Exception {
        Long clientId = 1L;
        ClientResponse client = new ClientResponse(clientId, "text 1");
        given(clientService.findClientById(clientId)).willReturn(Optional.of(client));

        this.mockMvc
                .perform(get("/api/clients/{id}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(client.text())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingClient() throws Exception {
        Long clientId = 1L;
        given(clientService.findClientById(clientId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/clients/{id}", clientId))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-api-archunit-sample.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Client with Id '%d' not found".formatted(clientId)));
    }

    @Test
    void shouldCreateNewClient() throws Exception {

        ClientResponse client = new ClientResponse(1L, "some text");
        ClientRequest clientRequest = new ClientRequest("some text");
        given(clientService.saveClient(any(ClientRequest.class))).willReturn(client);

        this.mockMvc
                .perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(clientRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(client.text())));
    }

    @Test
    void shouldReturn400WhenCreateNewClientWithoutText() throws Exception {
        ClientRequest clientRequest = new ClientRequest(null);

        this.mockMvc
                .perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(clientRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("https://archunit-sample.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/clients")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateClient() throws Exception {
        Long clientId = 1L;
        ClientResponse client = new ClientResponse(clientId, "Updated text");
        ClientRequest clientRequest = new ClientRequest("Updated text");
        given(clientService.updateClient(eq(clientId), any(ClientRequest.class)))
                .willReturn(client);

        this.mockMvc
                .perform(put("/api/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(clientRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(clientId), Long.class))
                .andExpect(jsonPath("$.text", is(client.text())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingClient() throws Exception {
        Long clientId = 1L;
        ClientRequest clientRequest = new ClientRequest("Updated text");
        given(clientService.updateClient(eq(clientId), any(ClientRequest.class)))
                .willThrow(new ClientNotFoundException(clientId));

        this.mockMvc
                .perform(put("/api/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(clientRequest)))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-api-archunit-sample.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Client with Id '%d' not found".formatted(clientId)));
    }

    @Test
    void shouldDeleteClient() throws Exception {
        Long clientId = 1L;
        ClientResponse client = new ClientResponse(clientId, "Some text");
        given(clientService.findClientById(clientId)).willReturn(Optional.of(client));
        doNothing().when(clientService).deleteClientById(clientId);

        this.mockMvc
                .perform(delete("/api/clients/{id}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(client.text())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingClient() throws Exception {
        Long clientId = 1L;
        given(clientService.findClientById(clientId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/clients/{id}", clientId))
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-api-archunit-sample.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Client with Id '%d' not found".formatted(clientId)));
    }

    List<ClientResponse> getClientResponseList() {
        return clientList.stream()
                .map(client -> new ClientResponse(client.getId(), client.getText()))
                .toList();
    }
}
