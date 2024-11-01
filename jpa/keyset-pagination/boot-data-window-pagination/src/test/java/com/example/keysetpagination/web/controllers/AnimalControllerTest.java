package com.example.keysetpagination.web.controllers;

import static com.example.keysetpagination.utils.AppConstants.PROFILE_TEST;
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

import com.example.keysetpagination.entities.Animal;
import com.example.keysetpagination.exception.AnimalNotFoundException;
import com.example.keysetpagination.model.query.FindAnimalsQuery;
import com.example.keysetpagination.model.request.AnimalRequest;
import com.example.keysetpagination.model.response.AnimalResponse;
import com.example.keysetpagination.model.response.PagedResult;
import com.example.keysetpagination.services.AnimalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AnimalController.class)
@ActiveProfiles(PROFILE_TEST)
class AnimalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnimalService animalService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<Animal> animalList;

    @BeforeEach
    void setUp() {
        this.animalList = new ArrayList<>();
        animalList.add(new Animal().setId(1L).setName("Lion"));
        animalList.add(new Animal().setId(2L).setName("Elephant"));
        animalList.add(new Animal().setId(3L).setName("Giraffe"));
    }

    @Test
    void shouldFetchAllAnimals() throws Exception {

        Page<Animal> page = new PageImpl<>(animalList);
        PagedResult<AnimalResponse> animalPagedResult = new PagedResult<>(page, getAnimalResponseList());
        FindAnimalsQuery findAnimalsQuery = new FindAnimalsQuery(0, 10, "id", "asc");
        given(animalService.findAllAnimals(findAnimalsQuery)).willReturn(animalPagedResult);

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
    void shouldFetchAllAnimalsWithCustomPageSize() throws Exception {
        FindAnimalsQuery findAnimalsQuery = new FindAnimalsQuery(1, 2, "id", "desc");
        Page<Animal> page = new PageImpl<>(
                animalList.subList(2, 3), PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "id")), animalList.size());
        PagedResult<AnimalResponse> animalPagedResult =
                new PagedResult<>(page, getAnimalResponseList().subList(2, 3));

        given(animalService.findAllAnimals(findAnimalsQuery)).willReturn(animalPagedResult);

        this.mockMvc
                .perform(get("/api/animals")
                        .param("pageNo", "1")
                        .param("pageSize", "2")
                        .param("sortBy", "id")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(1)))
                .andExpect(jsonPath("$.isFirst", is(false)));
    }

    @Test
    void shouldFindAnimalById() throws Exception {
        Long animalId = 1L;
        AnimalResponse animal = new AnimalResponse(animalId, "text 1");
        given(animalService.findAnimalById(animalId)).willReturn(Optional.of(animal));

        this.mockMvc
                .perform(get("/api/animals/{id}", animalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(animal.text())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingAnimal() throws Exception {
        Long animalId = 1L;
        given(animalService.findAnimalById(animalId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/animals/{id}", animalId))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.boot-data-window-pagination.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Animal with Id '%d' not found".formatted(animalId)));
    }

    @Test
    void shouldCreateNewAnimal() throws Exception {

        AnimalResponse animal = new AnimalResponse(1L, "Tiger");
        AnimalRequest animalRequest = new AnimalRequest("Tiger");
        given(animalService.saveAnimal(any(AnimalRequest.class))).willReturn(animal);

        this.mockMvc
                .perform(post("/api/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animalRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(animal.text())));
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
        Long animalId = 1L;
        AnimalResponse animal = new AnimalResponse(animalId, "Updated text");
        AnimalRequest animalRequest = new AnimalRequest("Updated text");
        given(animalService.updateAnimal(eq(animalId), any(AnimalRequest.class)))
                .willReturn(animal);

        this.mockMvc
                .perform(put("/api/animals/{id}", animalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(animalId), Long.class))
                .andExpect(jsonPath("$.text", is(animal.text())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingAnimal() throws Exception {
        Long animalId = 1L;
        AnimalRequest animalRequest = new AnimalRequest("Updated text");
        given(animalService.updateAnimal(eq(animalId), any(AnimalRequest.class)))
                .willThrow(new AnimalNotFoundException(animalId));

        this.mockMvc
                .perform(put("/api/animals/{id}", animalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animalRequest)))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.boot-data-window-pagination.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Animal with Id '%d' not found".formatted(animalId)));
    }

    @Test
    void shouldDeleteAnimal() throws Exception {
        Long animalId = 1L;
        AnimalResponse animal = new AnimalResponse(animalId, "Some text");
        given(animalService.findAnimalById(animalId)).willReturn(Optional.of(animal));
        doNothing().when(animalService).deleteAnimalById(animalId);

        this.mockMvc
                .perform(delete("/api/animals/{id}", animalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(animal.text())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingAnimal() throws Exception {
        Long animalId = 1L;
        given(animalService.findAnimalById(animalId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/animals/{id}", animalId))
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.boot-data-window-pagination.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Animal with Id '%d' not found".formatted(animalId)));
    }

    List<AnimalResponse> getAnimalResponseList() {
        return animalList.stream()
                .map(animal -> new AnimalResponse(animal.getId(), animal.getName()))
                .toList();
    }
}
