package com.example.locks.web.controllers;

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

import com.example.locks.common.AbstractIntegrationTest;
import com.example.locks.entities.Actor;
import com.example.locks.model.request.ActorRequest;
import com.example.locks.repositories.ActorRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class ActorControllerIT extends AbstractIntegrationTest {

    @Autowired
    private ActorRepository actorRepository;

    private List<Actor> actorList = null;

    @BeforeEach
    void setUp() {
        actorRepository.deleteAllInBatch();

        actorList = new ArrayList<>();
        actorList.add(new Actor(null, "First Actor"));
        actorList.add(new Actor(null, "Second Actor"));
        actorList.add(new Actor(null, "Third Actor"));
        actorList = actorRepository.saveAll(actorList);
    }

    @Test
    void shouldFetchAllActors() throws Exception {
        this.mockMvc
                .perform(get("/api/actors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(actorList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindActorById() throws Exception {
        Actor actor = actorList.get(0);
        Long actorId = actor.getId();

        this.mockMvc
                .perform(get("/api/actors/{id}", actorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(actor.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(actor.getText())));
    }

    @Test
    void shouldCreateNewActor() throws Exception {
        ActorRequest actorRequest = new ActorRequest("New Actor");
        this.mockMvc
                .perform(post("/api/actors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actorRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(actorRequest.text())));
    }

    @Test
    void shouldReturn400WhenCreateNewActorWithoutText() throws Exception {
        ActorRequest actorRequest = new ActorRequest(null);

        this.mockMvc
                .perform(post("/api/actors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actorRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/actors")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateActor() throws Exception {
        Long actorId = actorList.get(0).getId();
        ActorRequest actorRequest = new ActorRequest("Updated Actor");

        this.mockMvc
                .perform(put("/api/actors/{id}", actorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actorRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(actorId), Long.class))
                .andExpect(jsonPath("$.text", is(actorRequest.text())));
    }

    @Test
    void shouldDeleteActor() throws Exception {
        Actor actor = actorList.get(0);

        this.mockMvc
                .perform(delete("/api/actors/{id}", actor.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(actor.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(actor.getText())));
    }
}
