package com.scheduler.quartz.web.actuator;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
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
                .convertTo(JsonNode.class)
                .satisfies(jobs -> {
                    assertThat(jobs.has("groups")).isTrue();
                    JsonNode groups = jobs.get("groups");
                    assertThat(groups.has("sample-group")).isTrue();
                    JsonNode defaultGroup = groups.get("sample-group");
                    assertThat(defaultGroup.has("jobs")).isTrue();
                    JsonNode jobsArray = defaultGroup.get("jobs");
                    assertThat(jobsArray.isArray()).isTrue();
                    assertThat(jobsArray.size()).isEqualTo(1);
                    assertThat(jobsArray.get(0).asText()).isEqualTo("oddEvenJob");
                });

        // Get specific job details
        mockMvcTester
                .get()
                .uri("/actuator/quartz/jobs/{group}/{name}", "sample-group", "oddEvenJob")
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(JsonNode.class)
                .satisfies(job -> {
                    assertThat(job.has("group")).isTrue();
                    assertThat(job.get("group").asText()).isEqualTo("sample-group");
                    assertThat(job.has("name")).isTrue();
                    assertThat(job.get("name").asText()).isEqualTo("oddEvenJob");
                    assertThat(job.has("description")).isTrue();
                    assertThat(job.get("description").asText()).isEqualTo("Sample OddEvenJob");
                    assertThat(job.has("className")).isTrue();
                    assertThat(job.get("className").asText()).isEqualTo("com.scheduler.quartz.job.SampleJob");
                    assertThat(job.has("durable")).isTrue();
                    assertThat(job.get("durable").asBoolean()).isTrue();
                    assertThat(job.has("requestRecovery")).isTrue();
                    assertThat(job.get("requestRecovery").asBoolean()).isTrue();
                    assertThat(job.has("triggers")).isTrue();
                    JsonNode triggers = job.get("triggers");
                    assertThat(triggers.isArray()).isTrue();
                    assertThat(triggers.size()).isGreaterThan(0);
                    JsonNode trigger = triggers.get(0);
                    assertThat(trigger.has("group")).isTrue();
                    assertThat(trigger.get("group").asText()).isEqualTo("sample-group");
                    assertThat(trigger.has("name")).isTrue();
                    assertThat(trigger.get("name").asText()).isEqualTo("sample-job-trigger");
                    assertThat(trigger.has("nextFireTime")).isTrue();
                    assertThat(trigger.has("priority")).isTrue();
                });

        // Trigger the job
        mockMvcTester
                .post()
                .uri("/actuator/quartz/jobs/{group}/{name}", "sample-group", "oddEvenJob")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"state\":\"running\"}")
                .assertThat()
                .hasStatusOk()
                .hasContentType("application/vnd.spring-boot.actuator.v3+json")
                .bodyJson()
                .convertTo(JsonNode.class)
                .satisfies(jobs -> {
                    assertThat(jobs.has("group")).isTrue();
                    String groupName = jobs.get("group").asText();
                    assertThat(groupName).isEqualTo("sample-group");
                    assertThat(jobs.has("name")).isTrue();
                    String jobName = jobs.get("name").asText();
                    assertThat(jobName).isEqualTo("oddEvenJob");
                    assertThat(jobs.has("className")).isTrue();
                    String className = jobs.get("className").asText();
                    assertThat(className).isEqualTo("com.scheduler.quartz.job.SampleJob");
                    assertThat(jobs.has("triggerTime")).isTrue();
                    String triggerTime = jobs.get("triggerTime").asText();
                    assertThat(triggerTime).isNotEmpty();
                });
    }

    @Test
    void testQuartzActuatorEndpointsWithInvalidJob() {

        // Get non-existent job details should return 404
        mockMvcTester
                .get()
                .uri("/actuator/quartz/jobs/{group}/{name}", DEFAULT_GROUP, SAMPLE_JOB)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(404);

        // Trigger non-existent job should return 404
        mockMvcTester
                .post()
                .uri("/actuator/quartz/jobs/{group}/{name}", DEFAULT_GROUP, SAMPLE_JOB)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"state\":\"running\"}")
                .assertThat()
                .hasStatus(404);
    }
}
