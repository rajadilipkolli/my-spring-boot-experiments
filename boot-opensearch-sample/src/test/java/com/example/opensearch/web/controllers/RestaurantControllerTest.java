package com.example.opensearch.web.controllers;

import static com.example.opensearch.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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

import com.example.opensearch.entities.Restaurant;
import com.example.opensearch.model.response.PagedResult;
import com.example.opensearch.services.RestaurantService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = RestaurantController.class)
@ActiveProfiles(PROFILE_TEST)
class RestaurantControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private RestaurantService restaurantService;

    @Autowired private ObjectMapper objectMapper;

    private List<Restaurant> restaurantList;

    @BeforeEach
    void setUp() {
        this.restaurantList = new ArrayList<>();
        this.restaurantList.add(new Restaurant("1", "text 1"));
        this.restaurantList.add(new Restaurant("2", "text 2"));
        this.restaurantList.add(new Restaurant("3", "text 3"));
    }

    @Test
    void shouldFetchAllRestaurants() throws Exception {
        Page<Restaurant> page = new PageImpl<>(restaurantList);
        PagedResult<Restaurant> restaurantPagedResult = new PagedResult<>(page);
        given(restaurantService.findAllRestaurants(0, 10, "id", "asc"))
                .willReturn(restaurantPagedResult);

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
        Restaurant restaurant = new Restaurant(restaurantId, "text 1");
        given(restaurantService.findRestaurantById(restaurantId))
                .willReturn(Optional.of(restaurant));

        this.mockMvc
                .perform(get("/api/restaurants/{id}", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(restaurant.getName())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingRestaurant() throws Exception {
        String restaurantId = "1";
        given(restaurantService.findRestaurantById(restaurantId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/restaurants/{id}", restaurantId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewRestaurant() throws Exception {
        given(restaurantService.saveRestaurant(any(Restaurant.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        Restaurant restaurant = new Restaurant("1", "some text");
        this.mockMvc
                .perform(
                        post("/api/restaurants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(restaurant)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(restaurant.getName())));
    }

    @Test
    void shouldReturn400WhenCreateNewRestaurantWithoutText() throws Exception {
        Restaurant restaurant = new Restaurant(null, null);

        this.mockMvc
                .perform(
                        post("/api/restaurants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(restaurant)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/restaurants")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("name")))
                .andExpect(jsonPath("$.violations[0].message", is("Name cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateRestaurant() throws Exception {
        String restaurantId = "1";
        Restaurant restaurant = new Restaurant(restaurantId, "Updated text");
        given(restaurantService.findRestaurantById(restaurantId))
                .willReturn(Optional.of(restaurant));
        given(restaurantService.saveRestaurant(any(Restaurant.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/api/restaurants/{id}", restaurant.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(restaurant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(restaurant.getName())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingRestaurant() throws Exception {
        String restaurantId = "1";
        given(restaurantService.findRestaurantById(restaurantId)).willReturn(Optional.empty());
        Restaurant restaurant = new Restaurant(restaurantId, "Updated text");

        this.mockMvc
                .perform(
                        put("/api/restaurants/{id}", restaurantId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(restaurant)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteRestaurant() throws Exception {
        String restaurantId = "1";
        Restaurant restaurant = new Restaurant(restaurantId, "Some text");
        given(restaurantService.findRestaurantById(restaurantId))
                .willReturn(Optional.of(restaurant));
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

        this.mockMvc
                .perform(delete("/api/restaurants/{id}", restaurantId))
                .andExpect(status().isNotFound());
    }
}
