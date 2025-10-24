package com.example.jndi.web.controllers;

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

import com.example.jndi.common.AbstractIntegrationTest;
import com.example.jndi.entities.Driver;
import com.example.jndi.model.request.DriverRequest;
import com.example.jndi.repositories.DriverRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class DriverControllerIT extends AbstractIntegrationTest {

    @Autowired
    private DriverRepository driverRepository;

    private List<Driver> driverList = null;

    @BeforeEach
    void setUp() {
        driverRepository.deleteAllInBatch();

        driverList = new ArrayList<>();
        driverList.add(new Driver(null, "First Driver"));
        driverList.add(new Driver(null, "Second Driver"));
        driverList.add(new Driver(null, "Third Driver"));
        driverList = driverRepository.saveAll(driverList);
    }

    @Test
    void shouldFetchAllDrivers() throws Exception {
        this.mockMvc
                .perform(get("/api/drivers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(driverList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindDriverById() throws Exception {
        Driver driver = driverList.getFirst();
        Long driverId = driver.getId();

        this.mockMvc
                .perform(get("/api/drivers/{id}", driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(driver.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(driver.getText())));
    }

    @Test
    void shouldCreateNewDriver() throws Exception {
        DriverRequest driverRequest = new DriverRequest("New Driver");
        this.mockMvc
                .perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(driverRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(driverRequest.text())));
    }

    @Test
    void shouldReturn400WhenCreateNewDriverWithoutText() throws Exception {
        DriverRequest driverRequest = new DriverRequest(null);

        this.mockMvc
                .perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(driverRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.boot-jndi-sample.com/errors/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/drivers")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateDriver() throws Exception {
        Long driverId = driverList.getFirst().getId();
        DriverRequest driverRequest = new DriverRequest("Updated Driver");

        this.mockMvc
                .perform(put("/api/drivers/{id}", driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(driverRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(driverId), Long.class))
                .andExpect(jsonPath("$.text", is(driverRequest.text())));
    }

    @Test
    void shouldDeleteDriver() throws Exception {
        Driver driver = driverList.getFirst();

        this.mockMvc
                .perform(delete("/api/drivers/{id}", driver.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(driver.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(driver.getText())));
    }
}
