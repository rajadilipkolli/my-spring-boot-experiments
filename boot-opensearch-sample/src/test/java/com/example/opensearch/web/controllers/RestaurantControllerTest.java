package com.example.opensearch.web.controllers;

import static com.example.opensearch.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
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

import com.example.opensearch.entities.Address;
import com.example.opensearch.entities.Grades;
import com.example.opensearch.entities.Restaurant;
import com.example.opensearch.model.request.RestaurantRequest;
import com.example.opensearch.model.response.PagedResult;
import com.example.opensearch.services.RestaurantService;
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
import org.springframework.data.geo.Point;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = RestaurantController.class)
@ActiveProfiles(PROFILE_TEST)
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestaurantService restaurantService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<Restaurant> restaurantList;

    @BeforeEach
    void setUp() {
        this.restaurantList = new ArrayList<>();
        Address address = new Address();
        address.setLocation(new Point(-73.9, 40.8));
        Grades grade = new Grades("A", LocalDateTime.of(2022, 1, 1, 1, 1, 1), 15);
        Grades grade1 = new Grades("B", LocalDateTime.of(2022, 3, 31, 23, 59, 59), 15);
        this.restaurantList.add(new Restaurant("1", "text 1", "borough1", "cuisine1", address, List.of(grade, grade1)));
        this.restaurantList.add(new Restaurant("2", "text 2", "borough2", "cuisine2", address, List.of(grade, grade1)));
        this.restaurantList.add(new Restaurant("3", "text 3", "borough3", "cuisine3", address, List.of(grade, grade1)));
    }

    @Test
    void shouldFetchAllRestaurants() throws Exception {
        Page<Restaurant> page = new PageImpl<>(restaurantList);
        PagedResult<Restaurant> restaurantPagedResult = new PagedResult<>(page);
        given(restaurantService.findAllRestaurants(0, 10, "id", "asc")).willReturn(restaurantPagedResult);

        this.mockMvc
                .perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(restaurantList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindRestaurantById() throws Exception {
        String restaurantId = "1";
        Restaurant restaurant =
                new Restaurant(restaurantId, "text 1", "borough1", "cuisine1", new Address(), new ArrayList<>());
        given(restaurantService.findRestaurantById(restaurantId)).willReturn(Optional.of(restaurant));

        this.mockMvc
                .perform(get("/api/restaurants/{id}", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(restaurant.getName())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingRestaurant() throws Exception {
        String restaurantId = "1";
        given(restaurantService.findRestaurantById(restaurantId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/api/restaurants/{id}", restaurantId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewRestaurant() throws Exception {
        given(restaurantService.saveRestaurant(any(Restaurant.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        Restaurant restaurant =
                new Restaurant("1", "some text", "borough1", "cuisine1", new Address(), new ArrayList<>());
        this.mockMvc
                .perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(restaurant)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(restaurant.getName())));
    }

    @Test
    void shouldReturn400WhenCreateNewRestaurantWithoutRequiredFields() throws Exception {
        RestaurantRequest restaurant = new RestaurantRequest(null, null, null, null, new ArrayList<>());

        this.mockMvc
                .perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(restaurant)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.boot-opensearch.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/restaurants")))
                .andExpect(jsonPath("$.violations", hasSize(3)))
                .andExpect(jsonPath("$.violations[0].field", is("borough")))
                .andExpect(jsonPath("$.violations[0].message", is("Borough Can't be Blank")))
                .andExpect(jsonPath("$.violations[1].field", is("cuisine")))
                .andExpect(jsonPath("$.violations[1].message", is("Cuisine Can't be Blank")))
                .andExpect(jsonPath("$.violations[2].field", is("name")))
                .andExpect(jsonPath("$.violations[2].message", is("Name cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateRestaurant() throws Exception {
        String restaurantId = "1";
        Restaurant restaurant =
                new Restaurant(restaurantId, "Updated text", "borough1", "cuisine1", new Address(), new ArrayList<>());
        given(restaurantService.findRestaurantById(restaurantId)).willReturn(Optional.of(restaurant));
        given(restaurantService.saveRestaurant(any(Restaurant.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(put("/api/restaurants/{id}", restaurant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(restaurant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(restaurant.getName())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingRestaurant() throws Exception {
        String restaurantId = "1";
        given(restaurantService.findRestaurantById(restaurantId)).willReturn(Optional.empty());
        Restaurant restaurant =
                new Restaurant(restaurantId, "Updated text", "borough1", "cuisine1", new Address(), new ArrayList<>());

        this.mockMvc
                .perform(put("/api/restaurants/{id}", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(restaurant)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteRestaurant() throws Exception {
        String restaurantId = "1";
        Restaurant restaurant =
                new Restaurant(restaurantId, "Some text", "borough1", "cuisine1", new Address(), new ArrayList<>());
        given(restaurantService.findRestaurantById(restaurantId)).willReturn(Optional.of(restaurant));
        doNothing().when(restaurantService).deleteRestaurantById(restaurant.getId());

        this.mockMvc
                .perform(delete("/api/restaurants/{id}", restaurant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(restaurant.getName())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingRestaurant() throws Exception {
        String restaurantId = "1";
        given(restaurantService.findRestaurantById(restaurantId)).willReturn(Optional.empty());

        this.mockMvc.perform(delete("/api/restaurants/{id}", restaurantId)).andExpect(status().isNotFound());
    }
}
