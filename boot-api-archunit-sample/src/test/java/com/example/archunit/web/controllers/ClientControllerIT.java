package com.example.archunit.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.archunit.common.AbstractIntegrationTest;
import com.example.archunit.entities.Client;
import com.example.archunit.model.request.ClientRequest;
import com.example.archunit.repositories.ClientRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class ClientControllerIT extends AbstractIntegrationTest {

    @Autowired
    private ClientRepository clientRepository;

    private List<Client> clientList = null;

    @BeforeEach
    void setUp() {
        clientRepository.deleteAllInBatch();

        clientList = new ArrayList<>();
        clientList.add(new Client(null, "First Client"));
        clientList.add(new Client(null, "Second Client"));
        clientList.add(new Client(null, "Third Client"));
        clientList = clientRepository.saveAll(clientList);
    }

    @Test
    void shouldFetchAllClients() throws Exception {
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
        Client client = clientList.getFirst();
        Long clientId = client.getId();

        this.mockMvc
                .perform(get("/api/clients/{id}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(client.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(client.getText())));
    }

    @Test
    void shouldCreateNewClient() throws Exception {
        ClientRequest clientRequest = new ClientRequest("New Client");
        this.mockMvc
                .perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(clientRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(clientRequest.text())));
    }

    @Test
    void shouldReturn400WhenCreateNewClientWithoutText() throws Exception {
        ClientRequest clientRequest = new ClientRequest(null);

        this.mockMvc
                .perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(clientRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://archunit-sample.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/clients")))
                .andExpect(jsonPath("$.properties.violations", hasSize(1)))
                .andExpect(jsonPath("$.properties.violations[0].field", is("text")))
                .andExpect(jsonPath("$.properties.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateClient() throws Exception {
        Long clientId = clientList.getFirst().getId();
        ClientRequest clientRequest = new ClientRequest("Updated Client");

        this.mockMvc
                .perform(put("/api/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(clientRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(clientId), Long.class))
                .andExpect(jsonPath("$.text", is(clientRequest.text())));
    }

    @Test
    void shouldDeleteClient() throws Exception {
        Client client = clientList.getFirst();

        this.mockMvc
                .perform(delete("/api/clients/{id}", client.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(client.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(client.getText())));
    }
}
