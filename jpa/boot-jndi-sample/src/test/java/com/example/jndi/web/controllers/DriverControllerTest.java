package com.example.jndi.web.controllers;

import static com.example.jndi.utils.AppConstants.PROFILE_TEST;
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

import com.example.jndi.entities.Driver;
import com.example.jndi.exception.DriverNotFoundException;
import com.example.jndi.model.query.FindDriversQuery;
import com.example.jndi.model.request.DriverRequest;
import com.example.jndi.model.response.DriverResponse;
import com.example.jndi.model.response.PagedResult;
import com.example.jndi.services.DriverService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = DriverController.class)
@ActiveProfiles(PROFILE_TEST)
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DriverService driverService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<Driver> driverList;

    @BeforeEach
    void setUp() {
        this.driverList = new ArrayList<>();
        this.driverList.add(new Driver(1L, "text 1"));
        this.driverList.add(new Driver(2L, "text 2"));
        this.driverList.add(new Driver(3L, "text 3"));
    }

    @Test
    void shouldFetchAllDrivers() throws Exception {

        Page<Driver> page = new PageImpl<>(driverList);
        PagedResult<DriverResponse> driverPagedResult = new PagedResult<>(page, getDriverResponseList());
        FindDriversQuery findDriversQuery = new FindDriversQuery(0, 10, "id", "asc");
        given(driverService.findAllDrivers(findDriversQuery)).willReturn(driverPagedResult);

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
        Long driverId = 1L;
        DriverResponse driver = new DriverResponse(driverId, "text 1");
        given(driverService.findDriverById(driverId)).willReturn(Optional.of(driver));

        this.mockMvc
                .perform(get("/api/drivers/{id}", driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(driver.text())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingDriver() throws Exception {
        Long driverId = 1L;
        given(driverService.findDriverById(driverId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/drivers/{id}", driverId))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.boot-jndi-sample.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Driver with Id '%d' not found".formatted(driverId)));
    }

    @Test
    void shouldCreateNewDriver() throws Exception {

        DriverResponse driver = new DriverResponse(1L, "some text");
        DriverRequest driverRequest = new DriverRequest("some text");
        given(driverService.saveDriver(any(DriverRequest.class))).willReturn(driver);

        this.mockMvc
                .perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(driverRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(driver.text())));
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
        Long driverId = 1L;
        DriverResponse driver = new DriverResponse(driverId, "Updated text");
        DriverRequest driverRequest = new DriverRequest("Updated text");
        given(driverService.updateDriver(eq(driverId), any(DriverRequest.class)))
                .willReturn(driver);

        this.mockMvc
                .perform(put("/api/drivers/{id}", driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(driverRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(driverId), Long.class))
                .andExpect(jsonPath("$.text", is(driver.text())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingDriver() throws Exception {
        Long driverId = 1L;
        DriverRequest driverRequest = new DriverRequest("Updated text");
        given(driverService.updateDriver(eq(driverId), any(DriverRequest.class)))
                .willThrow(new DriverNotFoundException(driverId));

        this.mockMvc
                .perform(put("/api/drivers/{id}", driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(driverRequest)))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.boot-jndi-sample.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Driver with Id '%d' not found".formatted(driverId)));
    }

    @Test
    void shouldDeleteDriver() throws Exception {
        Long driverId = 1L;
        DriverResponse driver = new DriverResponse(driverId, "Some text");
        given(driverService.findDriverById(driverId)).willReturn(Optional.of(driver));
        doNothing().when(driverService).deleteDriverById(driverId);

        this.mockMvc
                .perform(delete("/api/drivers/{id}", driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(driver.text())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingDriver() throws Exception {
        Long driverId = 1L;
        given(driverService.findDriverById(driverId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/drivers/{id}", driverId))
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.boot-jndi-sample.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Driver with Id '%d' not found".formatted(driverId)));
    }

    List<DriverResponse> getDriverResponseList() {
        return driverList.stream()
                .map(driver -> new DriverResponse(driver.getId(), driver.getText()))
                .toList();
    }
}
