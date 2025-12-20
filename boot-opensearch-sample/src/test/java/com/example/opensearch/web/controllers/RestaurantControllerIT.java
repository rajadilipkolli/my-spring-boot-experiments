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
import com.example.opensearch.entities.Address;
import com.example.opensearch.entities.Grades;
import com.example.opensearch.entities.Restaurant;
import com.example.opensearch.model.request.RestaurantRequest;
import com.example.opensearch.repositories.RestaurantRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.http.MediaType;

class RestaurantControllerIT extends AbstractIntegrationTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    private List<Restaurant> restaurantList = null;

    @BeforeEach
    void setUp() {
        restaurantRepository.deleteAll();

        restaurantList = new ArrayList<>();
        Address address = new Address();
        address.setLocation(new Point(-73.9, 40.8));
        Grades grade = new Grades("A", LocalDateTime.of(2022, 1, 1, 1, 1, 1), 15);
        Grades grade1 = new Grades("B", LocalDateTime.of(2022, 3, 31, 23, 59, 59), 15);
        this.restaurantList.add(new Restaurant("1", "text 1", "borough1", "cuisine1", address, List.of(grade, grade1)));
        this.restaurantList.add(new Restaurant("2", "text 2", "borough2", "cuisine2", address, List.of(grade, grade1)));
        this.restaurantList.add(new Restaurant("3", "text 3", "borough3", "cuisine3", address, List.of(grade, grade1)));
        restaurantList = restaurantRepository.saveAll(restaurantList);
    }

    @Test
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
        Restaurant restaurant = restaurantList.getFirst();
        String restaurantId = restaurant.getId();

        this.mockMvc
                .perform(get("/api/restaurants/{id}", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(restaurant.getId()), String.class))
                .andExpect(jsonPath("$.name", is(restaurant.getName())));
    }

    @Test
    void shouldCreateNewRestaurant() throws Exception {
        Address address = new Address();
        address.setBuilding("building1");
        address.setZipcode(500049);
        address.setLocation(new Point(-73.8, 40.8));
        address.setStreet("street1");
        Grades grade = new Grades("1", LocalDateTime.now(), 5);
        RestaurantRequest restaurantRequest =
                new RestaurantRequest("New Restaurant", "borough1", "cuisine1", address, List.of(grade));
        this.mockMvc
                .perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(restaurantRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(restaurantRequest.name())));
    }

    @Test
    void shouldReturn400WhenCreateNewRestaurantWithoutText() throws Exception {
        RestaurantRequest restaurant = new RestaurantRequest(null, null, null, null, null);

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
        Restaurant restaurant = restaurantList.getFirst();
        restaurant.setName("Updated Restaurant");

        this.mockMvc
                .perform(put("/api/restaurants/{id}", restaurant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(restaurant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(restaurant.getId()), String.class))
                .andExpect(jsonPath("$.name", is(restaurant.getName())));
    }

    @Test
    void shouldDeleteRestaurant() throws Exception {
        Restaurant restaurant = restaurantList.getFirst();

        this.mockMvc
                .perform(delete("/api/restaurants/{id}", restaurant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(restaurant.getId()), String.class))
                .andExpect(jsonPath("$.name", is(restaurant.getName())));
    }
}
