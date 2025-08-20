package com.scheduler.quartz.actuator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.scheduler.quartz.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class QuartzActuatorEndpointIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testQuartzEndpoints() throws Exception {
        // List all jobs
        String response = mockMvc.perform(get("/actuator/quartz/jobs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isNotEmpty();

        // Get specific job details (replace with your actual job group and name)
        mockMvc.perform(get("/actuator/quartz/jobs/DEFAULT/sampleJob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.group").value("DEFAULT"))
                .andExpect(jsonPath("$.name").value("sampleJob"));

        // Pause a job
        mockMvc.perform(post("/actuator/quartz/jobs/DEFAULT/sampleJob/pause")).andExpect(status().isNoContent());

        // Resume a job
        mockMvc.perform(post("/actuator/quartz/jobs/DEFAULT/sampleJob/resume")).andExpect(status().isNoContent());

        // Trigger a job
        mockMvc.perform(post("/actuator/quartz/jobs/DEFAULT/sampleJob/trigger")).andExpect(status().isNoContent());
    }
}
