package com.example.opensearch.web.controllers;

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

import com.example.opensearch.common.AbstractIntegrationTest;
import com.example.opensearch.entities.Restaurant;
import com.example.opensearch.repositories.RestaurantRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class RestaurantControllerIT extends AbstractIntegrationTest {

    @Autowired private RestaurantRepository restaurantRepository;

    private List<Restaurant> restaurantList = null;

    @BeforeEach
    void setUp() {
        restaurantRepository.deleteAll();

        restaurantList = new ArrayList<>();
        restaurantList.add(new Restaurant(null, "First Restaurant"));
        restaurantList.add(new Restaurant(null, "Second Restaurant"));
        restaurantList.add(new Restaurant(null, "Third Restaurant"));
        restaurantList = restaurantRepository.saveAll(restaurantList);
    }

    @Test
    @Disabled("should fix mapping for id column")
    void shouldFetchAllRestaurants() throws Exception {
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
        Restaurant restaurant = restaurantList.get(0);
        String restaurantId = restaurant.getId();

        this.mockMvc
                .perform(get("/api/restaurants/{id}", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(restaurant.getId()), String.class))
                .andExpect(jsonPath("$.name", is(restaurant.getName())));
    }

    @Test
    void shouldCreateNewRestaurant() throws Exception {
        Restaurant restaurant = new Restaurant(null, "New Restaurant");
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
        Restaurant restaurant = restaurantList.get(0);
        restaurant.setName("Updated Restaurant");

        this.mockMvc
                .perform(
                        put("/api/restaurants/{id}", restaurant.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(restaurant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(restaurant.getId()), String.class))
                .andExpect(jsonPath("$.name", is(restaurant.getName())));
    }

    @Test
    void shouldDeleteRestaurant() throws Exception {
        Restaurant restaurant = restaurantList.get(0);

        this.mockMvc
                .perform(delete("/api/restaurants/{id}", restaurant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(restaurant.getId()), String.class))
                .andExpect(jsonPath("$.name", is(restaurant.getName())));
    }
}
