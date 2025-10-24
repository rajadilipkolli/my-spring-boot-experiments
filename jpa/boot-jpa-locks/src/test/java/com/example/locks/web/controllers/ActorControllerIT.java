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
import java.time.LocalDate;
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
        actorList.add(
                new Actor().setActorName("First Actor").setDob(LocalDate.now().minusYears(30)));
        actorList.add(new Actor().setActorName("Second Actor").setNationality("Indian"));
        actorList.add(new Actor().setActorName("Third Actor").setMovies(new ArrayList<>()));
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
        Actor actor = actorList.getFirst();
        Long actorId = actor.getActorId();

        this.mockMvc
                .perform(get("/api/actors/{id}", actorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actorId", is(actor.getActorId()), Long.class))
                .andExpect(jsonPath("$.actorName", is(actor.getActorName())));
    }

    @Test
    void shouldCreateNewActor() throws Exception {
        ActorRequest actorRequest =
                new ActorRequest("New Actor", LocalDate.now().minusYears(50), "Indian");
        this.mockMvc
                .perform(post("/api/actors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actorRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.actorId", notNullValue()))
                .andExpect(jsonPath("$.actorName", is(actorRequest.actorName())));
    }

    @Test
    void shouldReturn400WhenCreateNewActorWithoutActorName() throws Exception {
        ActorRequest actorRequest = new ActorRequest(null, null, null);

        this.mockMvc
                .perform(post("/api/actors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actorRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.boot-jpa-locks.com/errors/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/actors")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("actorName")))
                .andExpect(jsonPath("$.violations[0].message", is("ActorName cant be Blank")))
                .andReturn();
    }

    @Test
    void shouldUpdateActor() throws Exception {
        Actor actor = actorList.getFirst();
        ActorRequest actorRequest = new ActorRequest("Updated Actor", actor.getDob(), actor.getNationality());

        this.mockMvc
                .perform(put("/api/actors/{id}", actor.getActorId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actorRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actorId", is(actor.getActorId()), Long.class))
                .andExpect(jsonPath("$.actorName", is(actorRequest.actorName())));
    }

    @Test
    void shouldDeleteActor() throws Exception {
        Actor actor = actorList.getFirst();

        this.mockMvc
                .perform(delete("/api/actors/{id}", actor.getActorId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actorId", is(actor.getActorId()), Long.class))
                .andExpect(jsonPath("$.actorName", is(actor.getActorName())));
    }
}
