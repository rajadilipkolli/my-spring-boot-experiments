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
import com.example.keysetpagination.repositories.CustomWindow;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        animalList.add(new Animal().setName("Lion").setType("Mammal").setHabitat("Savannah"));
        animalList.add(new Animal().setName("Elephant").setType("Mammal").setHabitat("Forest"));
        animalList.add(new Animal().setName("Shark").setType("Fish").setHabitat("Ocean"));
        animalList.add(new Animal().setName("Parrot").setType("Bird").setHabitat("Rainforest"));
        animalList.add(new Animal().setName("Penguin").setType("Bird").setHabitat("Antarctic"));
        animalList.add(new Animal().setName("Crocodile").setType("Reptile").setHabitat("Swamp"));
        animalList.add(new Animal().setName("Frog").setType("Amphibian").setHabitat("Wetlands"));
        animalList.add(new Animal().setName("Eagle").setType("Bird").setHabitat("Mountains"));
        animalList.add(new Animal().setName("Whale").setType("Mammal").setHabitat("Ocean"));
        animalList.add(new Animal().setName("Snake").setType("Reptile").setHabitat("Desert"));
        animalList = animalRepository.saveAll(animalList);
    }

    @Test
    void shouldFetchAllAnimals() throws Exception {
        this.mockMvc
                .perform(get("/api/animals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(animalList.size())))
                .andExpect(jsonPath("$.totalElements", is(10)))
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
    void shouldSearchAnimals() throws Exception {
        String contentAsString = this.mockMvc
                .perform(get("/api/animals/search").param("pageSize", "2").param("type", "Bird"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(2)))
                .andExpect(jsonPath("$.content[0].type", is("Bird")))
                .andExpect(jsonPath("$.content[1].type", is("Bird")))
                .andExpect(jsonPath("$.last", is(false)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        CustomWindow<Map<String, String>> window = objectMapper.readValue(contentAsString, CustomWindow.class);
        List<Map<String, String>> animalResponses = window.getContent();
        Map<String, String> animalResponsesLast = animalResponses.getLast();
        this.mockMvc
                .perform(get("/api/animals/search")
                        .param("pageSize", "2")
                        .param("type", "Bird")
                        .param("scrollId", String.valueOf(animalResponsesLast.get("id"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(1)))
                .andExpect(jsonPath("$.content[0].type", is("Bird")))
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void shouldReturnEmptyResultForNonExistentType() throws Exception {
        this.mockMvc
                .perform(get("/api/animals/search").param("type", "NonExistentType"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.last", is(true)));
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
        AnimalRequest animalRequest = new AnimalRequest("Snake", "Reptile", "Desert");
        this.mockMvc
                .perform(post("/api/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animalRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(animalRequest.name())))
                .andExpect(jsonPath("$.type", is(animalRequest.type())))
                .andExpect(jsonPath("$.habitat", is(animalRequest.habitat())));
    }

    @Test
    void shouldReturn400WhenCreateNewAnimalWithoutNameAndType() throws Exception {
        AnimalRequest animalRequest = new AnimalRequest(null, null, null);

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
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("name")))
                .andExpect(jsonPath("$.violations[0].message", is("Name cannot be blank")))
                .andExpect(jsonPath("$.violations[1].field", is("type")))
                .andExpect(jsonPath("$.violations[1].message", is("Type cannot be blank")))
                .andReturn();
    }

    @Test
    void shouldUpdateAnimal() throws Exception {
        Animal animal = animalList.getFirst();
        AnimalRequest animalRequest = new AnimalRequest("Updated Animal", animal.getType(), animal.getHabitat());

        Long animalId = animal.getId();
        this.mockMvc
                .perform(put("/api/animals/{id}", animalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(animalId), Long.class))
                .andExpect(jsonPath("$.name", is(animalRequest.name())))
                .andExpect(jsonPath("$.type", is(animalRequest.type())))
                .andExpect(jsonPath("$.habitat", is(animalRequest.habitat())));
    }

    @Test
    void shouldBeIdempotentWhenUpdatingAnimalWithSameData() throws Exception {
        Long animalId = animalList.getFirst().getId();
        AnimalRequest animalRequest = new AnimalRequest("Elephant", "Mammal", "Forest");

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
        AnimalRequest animalRequest = new AnimalRequest("Updated Animal", "Updated Type", "Forest");

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
