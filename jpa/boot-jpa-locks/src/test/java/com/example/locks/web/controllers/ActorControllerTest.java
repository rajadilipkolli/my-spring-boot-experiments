package com.example.locks.web.controllers;

import static com.example.locks.utils.AppConstants.PROFILE_TEST;
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

import com.example.locks.entities.Actor;
import com.example.locks.exception.ActorNotFoundException;
import com.example.locks.model.query.FindActorsQuery;
import com.example.locks.model.request.ActorRequest;
import com.example.locks.model.response.ActorResponse;
import com.example.locks.model.response.PagedResult;
import com.example.locks.services.ActorService;
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

@WebMvcTest(controllers = ActorController.class)
@ActiveProfiles(PROFILE_TEST)
class ActorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActorService actorService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<Actor> actorList;

    @BeforeEach
    void setUp() {
        this.actorList = new ArrayList<>();
        this.actorList.add(new Actor(1L, "text 1"));
        this.actorList.add(new Actor(2L, "text 2"));
        this.actorList.add(new Actor(3L, "text 3"));
    }

    @Test
    void shouldFetchAllActors() throws Exception {

        Page<Actor> page = new PageImpl<>(actorList);
        PagedResult<ActorResponse> actorPagedResult = new PagedResult<>(page, getActorResponseList());
        FindActorsQuery findActorsQuery = new FindActorsQuery(0, 10, "id", "asc");
        given(actorService.findAllActors(findActorsQuery)).willReturn(actorPagedResult);

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
        Long actorId = 1L;
        ActorResponse actor = new ActorResponse(actorId, "text 1");
        given(actorService.findActorById(actorId)).willReturn(Optional.of(actor));

        this.mockMvc
                .perform(get("/api/actors/{id}", actorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(actor.text())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingActor() throws Exception {
        Long actorId = 1L;
        given(actorService.findActorById(actorId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/actors/{id}", actorId))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-jpa-locks.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Actor with Id '%d' not found".formatted(actorId)));
    }

    @Test
    void shouldCreateNewActor() throws Exception {

        ActorResponse actor = new ActorResponse(1L, "some text");
        ActorRequest actorRequest = new ActorRequest("some text");
        given(actorService.saveActor(any(ActorRequest.class))).willReturn(actor);

        this.mockMvc
                .perform(post("/api/actors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actorRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(actor.text())));
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
        Long actorId = 1L;
        ActorResponse actor = new ActorResponse(actorId, "Updated text");
        ActorRequest actorRequest = new ActorRequest("Updated text");
        given(actorService.updateActor(eq(actorId), any(ActorRequest.class))).willReturn(actor);

        this.mockMvc
                .perform(put("/api/actors/{id}", actorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actorRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(actorId), Long.class))
                .andExpect(jsonPath("$.text", is(actor.text())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingActor() throws Exception {
        Long actorId = 1L;
        ActorRequest actorRequest = new ActorRequest("Updated text");
        given(actorService.updateActor(eq(actorId), any(ActorRequest.class)))
                .willThrow(new ActorNotFoundException(actorId));

        this.mockMvc
                .perform(put("/api/actors/{id}", actorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actorRequest)))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-jpa-locks.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Actor with Id '%d' not found".formatted(actorId)));
    }

    @Test
    void shouldDeleteActor() throws Exception {
        Long actorId = 1L;
        ActorResponse actor = new ActorResponse(actorId, "Some text");
        given(actorService.findActorById(actorId)).willReturn(Optional.of(actor));
        doNothing().when(actorService).deleteActorById(actorId);

        this.mockMvc
                .perform(delete("/api/actors/{id}", actorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(actor.text())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingActor() throws Exception {
        Long actorId = 1L;
        given(actorService.findActorById(actorId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/actors/{id}", actorId))
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-jpa-locks.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Actor with Id '%d' not found".formatted(actorId)));
    }

    List<ActorResponse> getActorResponseList() {
        return actorList.stream()
                .map(actor -> new ActorResponse(actor.getId(), actor.getText()))
                .toList();
    }
}
