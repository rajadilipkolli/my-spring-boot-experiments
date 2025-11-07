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
import com.example.keysetpagination.model.query.SearchRequest;
import com.example.keysetpagination.model.request.AnimalRequest;
import com.example.keysetpagination.model.response.AnimalResponse;
import com.example.keysetpagination.model.response.PagedResult;
import com.example.keysetpagination.services.AnimalService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = AnimalController.class)
@ActiveProfiles(PROFILE_TEST)
class AnimalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnimalService animalService;

    @Autowired
    private JsonMapper jsonMapper;

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
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(2)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.isFirst", is(false)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(true)));
    }

    @Test
    void shouldReturnBadRequestWhenPageSizeIsLessThanMin() throws Exception {
        SearchRequest searchRequest = new SearchRequest();
        mockMvc.perform(post("/api/animals/search")
                        .param("pageSize", "0") // Less than minimum value of 1
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.boot-data-window-pagination.com/errors/validation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail").value("searchAnimals.pageSize: must be greater than or equal to 1"))
                .andExpect(jsonPath("$.instance", is("/api/animals/search")))
                .andExpect(jsonPath("$.errorCategory", is("Validation")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void shouldReturnBadRequestWhenPageSizeExceedsMax() throws Exception {
        SearchRequest searchRequest = new SearchRequest();
        mockMvc.perform(post("/api/animals/search")
                        .param("pageSize", "101") // Exceeds maximum value of 100
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.boot-data-window-pagination.com/errors/validation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail").value("searchAnimals.pageSize: must be less than or equal to 100"))
                .andExpect(jsonPath("$.instance", is("/api/animals/search")))
                .andExpect(jsonPath("$.errorCategory", is("Validation")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void shouldReturnOkWhenPageSizeIsWithinValidRange() throws Exception {
        SearchRequest searchRequest = new SearchRequest();
        mockMvc.perform(post("/api/animals/search")
                        .param("pageSize", "50") // Within valid range (1-100)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFindAnimalById() throws Exception {
        Long animalId = 1L;
        AnimalResponse animal = new AnimalResponse(animalId, "name 1", "junitType", "junitHabitat", LocalDateTime.MAX);
        given(animalService.findAnimalById(animalId)).willReturn(Optional.of(animal));

        this.mockMvc
                .perform(get("/api/animals/{id}", animalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(animal.name())));
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

        AnimalResponse animalResponse = new AnimalResponse(1L, "Tiger", "junitType", "junitHabitat", LocalDateTime.MAX);
        AnimalRequest animalRequest = new AnimalRequest("Tiger", "junitType", "junitHabitat");
        given(animalService.saveAnimal(any(AnimalRequest.class))).willReturn(animalResponse);

        this.mockMvc
                .perform(post("/api/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(animalRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(animalResponse.name())));
    }

    @Test
    void shouldReturn400WhenCreateNewAnimalWithoutNameAndType() throws Exception {
        AnimalRequest animalRequest = new AnimalRequest(null, null, null);

        this.mockMvc
                .perform(post("/api/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(animalRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.boot-data-window-pagination.com/errors/validation")))
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
        Long animalId = 1L;
        AnimalResponse animalResponse =
                new AnimalResponse(animalId, "Updated name", "junitType", "junitHabitat", LocalDateTime.MAX);
        AnimalRequest animalRequest = new AnimalRequest("Updated name", "junitType", "junitHabitat");
        given(animalService.updateAnimal(eq(animalId), any(AnimalRequest.class)))
                .willReturn(animalResponse);

        this.mockMvc
                .perform(put("/api/animals/{id}", animalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(animalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(animalId), Long.class))
                .andExpect(jsonPath("$.name", is(animalResponse.name())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingAnimal() throws Exception {
        Long animalId = 1L;
        AnimalRequest animalRequest = new AnimalRequest("Updated name", "junitType", "junitHabitat");
        given(animalService.updateAnimal(eq(animalId), any(AnimalRequest.class)))
                .willThrow(new AnimalNotFoundException(animalId));

        this.mockMvc
                .perform(put("/api/animals/{id}", animalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(animalRequest)))
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
        AnimalResponse animal =
                new AnimalResponse(animalId, "Some name", "junitType", "junitHabitat", LocalDateTime.MAX);
        given(animalService.findAnimalById(animalId)).willReturn(Optional.of(animal));
        doNothing().when(animalService).deleteAnimalById(animalId);

        this.mockMvc
                .perform(delete("/api/animals/{id}", animalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(animal.name())));
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
                .map(animal -> new AnimalResponse(
                        animal.getId(), animal.getName(), animal.getType(), animal.getHabitat(), animal.getCreated()))
                .toList();
    }
}
