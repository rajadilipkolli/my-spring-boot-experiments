package com.example.keysetpagination.web.controllers;

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

import com.example.keysetpagination.common.AbstractIntegrationTest;
import com.example.keysetpagination.entities.Animal;
import com.example.keysetpagination.model.request.AnimalRequest;
import com.example.keysetpagination.repositories.AnimalRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class AnimalControllerIT extends AbstractIntegrationTest {

    @Autowired
    private AnimalRepository animalRepository;

    private List<Animal> animalList = null;

    @BeforeEach
    void setUp() {
        animalRepository.deleteAllInBatch();

        animalList = new ArrayList<>();
        animalList.add(new Animal().setName("Lion"));
        animalList.add(new Animal().setName("Elephant"));
        animalList.add(new Animal().setName("Giraffe"));
        animalList = animalRepository.saveAll(animalList);
    }

    @Test
    void shouldFetchAllAnimals() throws Exception {
        this.mockMvc
                .perform(get("/api/animals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(animalList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAnimalsWithCustomPageSize() throws Exception {
        this.mockMvc
                .perform(get("/api/animals").param("pageSize", "2").param("pageNo", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(2)))
                .andExpect(jsonPath("$.hasNext", is(true)));
    }

    @Test
    void shouldFindAnimalById() throws Exception {
        Animal animal = animalList.getFirst();
        Long animalId = animal.getId();

        this.mockMvc
                .perform(get("/api/animals/{id}", animalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(animal.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(animal.getName())));
    }

    @Test
    void shouldCreateNewAnimal() throws Exception {
        AnimalRequest animalRequest = new AnimalRequest("New Animal");
        this.mockMvc
                .perform(post("/api/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animalRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(animalRequest.name())));
    }

    @Test
    void shouldReturn400WhenCreateNewAnimalWithoutText() throws Exception {
        AnimalRequest animalRequest = new AnimalRequest(null);

        this.mockMvc
                .perform(post("/api/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animalRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/animals")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("name")))
                .andExpect(jsonPath("$.violations[0].message", is("Name cannot be blank")))
                .andReturn();
    }

    @Test
    void shouldUpdateAnimal() throws Exception {
        Long animalId = animalList.getFirst().getId();
        AnimalRequest animalRequest = new AnimalRequest("Updated Animal");

        this.mockMvc
                .perform(put("/api/animals/{id}", animalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(animalId), Long.class))
                .andExpect(jsonPath("$.name", is(animalRequest.name())));
    }

    @Test
    void shouldBeIdempotentWhenUpdatingAnimalWithSameData() throws Exception {
        Long animalId = animalList.getFirst().getId();
        AnimalRequest animalRequest = new AnimalRequest("Elephant");

        // Perform update twice with same data
        this.mockMvc
                .perform(put("/api/animals/{id}", animalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animalRequest)))
                .andExpect(status().isOk());

        this.mockMvc
                .perform(put("/api/animals/{id}", animalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animalRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingAnimal() throws Exception {
        AnimalRequest animalRequest = new AnimalRequest("Updated Animal");

        this.mockMvc
                .perform(put("/api/animals/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animalRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void shouldDeleteAnimal() throws Exception {
        Animal animal = animalList.getFirst();
        Long animalId = animal.getId();

        this.mockMvc
                .perform(delete("/api/animals/{id}", animalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(animal.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(animal.getName())));

        // Verify animal is deleted
        this.mockMvc.perform(get("/api/animals/{id}", animalId)).andExpect(status().isNotFound());
    }
}
