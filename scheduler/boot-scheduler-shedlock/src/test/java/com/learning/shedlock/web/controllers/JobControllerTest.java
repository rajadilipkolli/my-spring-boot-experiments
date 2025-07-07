package com.learning.shedlock.web.controllers;

import static com.learning.shedlock.utils.AppConstants.PROFILE_TEST;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.shedlock.entities.Job;
import com.learning.shedlock.exception.JobNotFoundException;
import com.learning.shedlock.model.query.FindJobsQuery;
import com.learning.shedlock.model.request.JobRequest;
import com.learning.shedlock.model.response.JobResponse;
import com.learning.shedlock.model.response.PagedResult;
import com.learning.shedlock.services.JobService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = JobController.class)
@ActiveProfiles(PROFILE_TEST)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobService jobService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<Job> jobList;

    @BeforeEach
    void setUp() {
        this.jobList = new ArrayList<>();
        this.jobList.add(new Job().setId(1L).setText("text 1"));
        this.jobList.add(new Job().setId(2L).setText("text 2"));
        this.jobList.add(new Job().setId(3L).setText("text 3"));
    }

    @Test
    void shouldFetchAllJobs() throws Exception {

        Page<Job> page = new PageImpl<>(jobList);
        PagedResult<JobResponse> jobPagedResult = new PagedResult<>(page, getJobResponseList());
        FindJobsQuery findJobsQuery = new FindJobsQuery(0, 10, "id", "asc");
        given(jobService.findAllJobs(findJobsQuery)).willReturn(jobPagedResult);

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
        Long jobId = 1L;
        JobResponse job = new JobResponse(jobId, "text 1");
        given(jobService.findJobById(jobId)).willReturn(Optional.of(job));

        this.mockMvc
                .perform(get("/api/jobs/{id}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(job.text())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingJob() throws Exception {
        Long jobId = 1L;
        given(jobService.findJobById(jobId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/jobs/{id}", jobId))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-shedlock-sample.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Job with Id '%d' not found".formatted(jobId)));
    }

    @Test
    void shouldCreateNewJob() throws Exception {

        JobResponse job = new JobResponse(1L, "some text");
        JobRequest jobRequest = new JobRequest("some text");
        given(jobService.saveJob(any(JobRequest.class))).willReturn(job);

        this.mockMvc
                .perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(job.text())));
    }

    @Test
    void shouldReturn400WhenCreateNewJobWithoutText() throws Exception {
        JobRequest jobRequest = new JobRequest(null);

        this.mockMvc
                .perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
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
        Long jobId = 1L;
        JobResponse job = new JobResponse(jobId, "Updated text");
        JobRequest jobRequest = new JobRequest("Updated text");
        given(jobService.updateJob(eq(jobId), any(JobRequest.class))).willReturn(job);

        this.mockMvc
                .perform(put("/api/jobs/{id}", jobId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(jobId), Long.class))
                .andExpect(jsonPath("$.text", is(job.text())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingJob() throws Exception {
        Long jobId = 1L;
        JobRequest jobRequest = new JobRequest("Updated text");
        given(jobService.updateJob(eq(jobId), any(JobRequest.class))).willThrow(new JobNotFoundException(jobId));

        this.mockMvc
                .perform(put("/api/jobs/{id}", jobId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobRequest)))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-shedlock-sample.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Job with Id '%d' not found".formatted(jobId)));
    }

    @Test
    void shouldDeleteJob() throws Exception {
        Long jobId = 1L;
        JobResponse job = new JobResponse(jobId, "Some text");
        given(jobService.findJobById(jobId)).willReturn(Optional.of(job));
        doNothing().when(jobService).deleteJobById(jobId);

        this.mockMvc
                .perform(delete("/api/jobs/{id}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(job.text())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingJob() throws Exception {
        Long jobId = 1L;
        given(jobService.findJobById(jobId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/jobs/{id}", jobId))
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-shedlock-sample.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Job with Id '%d' not found".formatted(jobId)));
    }

    List<JobResponse> getJobResponseList() {
        return jobList.stream()
                .map(job -> new JobResponse(job.getId(), job.getText()))
                .toList();
    }
}
