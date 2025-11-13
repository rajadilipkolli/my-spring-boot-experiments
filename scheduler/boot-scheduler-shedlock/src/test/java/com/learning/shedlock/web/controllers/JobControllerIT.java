package com.learning.shedlock.web.controllers;

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

import com.learning.shedlock.common.AbstractIntegrationTest;
import com.learning.shedlock.entities.Job;
import com.learning.shedlock.model.request.JobRequest;
import com.learning.shedlock.repositories.JobRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class JobControllerIT extends AbstractIntegrationTest {

    @Autowired
    private JobRepository jobRepository;

    private List<Job> jobList = null;

    @BeforeEach
    void setUp() {
        jobRepository.deleteAllInBatch();

        jobList = new ArrayList<>();
        jobList.add(new Job().setText("First Job"));
        jobList.add(new Job().setText("Second Job"));
        jobList.add(new Job().setText("Third Job"));
        jobList = jobRepository.saveAll(jobList);
    }

    @Test
    void shouldFetchAllJobs() throws Exception {
        this.mockMvc
                .perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(jobList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindJobById() throws Exception {
        Job job = jobList.getFirst();
        Long jobId = job.getId();

        this.mockMvc
                .perform(get("/api/jobs/{id}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(job.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(job.getText())));
    }

    @Test
    void shouldCreateNewJob() throws Exception {
        JobRequest jobRequest = new JobRequest("New Job");
        this.mockMvc
                .perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(jobRequest.text())));
    }

    @Test
    void shouldReturn400WhenCreateNewJobWithoutText() throws Exception {
        JobRequest jobRequest = new JobRequest(null);

        this.mockMvc
                .perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-shedlock-sample.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/jobs")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateJob() throws Exception {
        Long jobId = jobList.getFirst().getId();
        JobRequest jobRequest = new JobRequest("Updated Job");

        this.mockMvc
                .perform(put("/api/jobs/{id}", jobId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(jobId), Long.class))
                .andExpect(jsonPath("$.text", is(jobRequest.text())));
    }

    @Test
    void shouldDeleteJob() throws Exception {
        Job job = jobList.getFirst();

        this.mockMvc
                .perform(delete("/api/jobs/{id}", job.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(job.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(job.getText())));
    }
}
