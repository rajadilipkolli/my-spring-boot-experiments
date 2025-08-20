package com.scheduler.quartz.web.actuator;

import com.scheduler.quartz.common.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class QuartzActuatorEndpointIntTest extends AbstractIntegrationTest {

    private static final String DEFAULT_GROUP = "DEFAULT";
    private static final String SAMPLE_JOB = "SampleJob";

    @BeforeEach
    void setup() {
        // Create a job first using the saveOrUpdate endpoint
        String requestBody =
                """
                {
                    "jobName": "SampleJob",
                    "cronExpression": "0/5 * * * * ?",
                    "jobId": "12345",
                    "description": "Test job description"
                }
                """;

        mockMvcTester
                .post()
                .uri("/api/saveOrUpdate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk();
    }

    @Test
    void testQuartzActuatorEndpoints() {
        // List all jobs
        mockMvcTester
                .get()
                .uri("/actuator/quartz/jobs")
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .path("$[0].group")
                .isEqualTo(DEFAULT_GROUP)
                .path("$[0].name")
                .isEqualTo(SAMPLE_JOB);

        // Get specific job details
        mockMvcTester
                .get()
                .uri("/actuator/quartz/jobs/{group}/{name}", DEFAULT_GROUP, SAMPLE_JOB)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .path("$.group")
                .isEqualTo(DEFAULT_GROUP)
                .path("$.name")
                .isEqualTo(SAMPLE_JOB);

        // Pause the job
        mockMvcTester
                .post()
                .uri("/actuator/quartz/jobs/{group}/{name}/pause", DEFAULT_GROUP, SAMPLE_JOB)
                .assertThat()
                .hasStatusNoContent();

        // Resume the job
        mockMvcTester
                .post()
                .uri("/actuator/quartz/jobs/{group}/{name}/resume", DEFAULT_GROUP, SAMPLE_JOB)
                .assertThat()
                .hasStatusNoContent();

        // Trigger the job
        mockMvcTester
                .post()
                .uri("/actuator/quartz/jobs/{group}/{name}/trigger", DEFAULT_GROUP, SAMPLE_JOB)
                .assertThat()
                .hasStatusNoContent();
    }

    @Test
    void testQuartzActuatorEndpointsWithInvalidJob() {
        String invalidJobName = "InvalidJob";

        // Get non-existent job details should return 404
        mockMvcTester
                .get()
                .uri("/actuator/quartz/jobs/{group}/{name}", DEFAULT_GROUP, invalidJobName)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(404);

        // Pause non-existent job should return 404
        mockMvcTester
                .post()
                .uri("/actuator/quartz/jobs/{group}/{name}/pause", DEFAULT_GROUP, invalidJobName)
                .assertThat()
                .hasStatus(404);

        // Resume non-existent job should return 404
        mockMvcTester
                .post()
                .uri("/actuator/quartz/jobs/{group}/{name}/resume", DEFAULT_GROUP, invalidJobName)
                .assertThat()
                .hasStatus(404);

        // Trigger non-existent job should return 404
        mockMvcTester
                .post()
                .uri("/actuator/quartz/jobs/{group}/{name}/trigger", DEFAULT_GROUP, invalidJobName)
                .assertThat()
                .hasStatus(404);
    }
}
