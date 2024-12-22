package com.scheduler.quartz.web.controller;

import com.scheduler.quartz.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class JobsControllerIntTest extends AbstractIntegrationTest {

    @Test
    void testGetJobs() {
        mockMvcTester.get().uri("/api").assertThat().hasStatusOk().hasContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void testGetJobsStatuses() {
        mockMvcTester.get().uri("/api/statuses").assertThat().hasStatusOk().hasContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void testSaveOrUpdate() {
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
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void testSaveOrUpdateWithInvalidCronExpression() {
        String requestBody =
                """
                    {
                        "jobName": "SampleJob",
                        "cronExpression": "0/5 * * * *",
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
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void testPauseJob() {
        String requestBody =
                """
                    {
                        "jobName": "SampleJob",
                        "jobId": "12345"
                    }
                """;

        mockMvcTester
                .post()
                .uri("/api/pauseJob")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void testPauseJobWithInvalidJobName() {
        String requestBody =
                """
                    {
                        "jobName": "InvalidJob",
                        "jobId": "12345"
                    }
                """;

        mockMvcTester
                .post()
                .uri("/api/pauseJob")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void testResumeJob() {
        String requestBody =
                """
                    {
                        "jobName": "SampleJob",
                        "jobId": "12345"
                    }
                """;
        mockMvcTester
                .post()
                .uri("/api/resumeJob")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void testResumeJobWithInvalidJobName() {
        String requestBody =
                """
                    {
                        "jobName": "InvalidJob",
                        "jobId": "12345"
                    }
                """;
        mockMvcTester
                .post()
                .uri("/api/resumeJob")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void testRunJob() {
        String requestBody =
                """
                {
                    "jobName": "SampleJob",
                    "jobId": "12345"
                }
            """;

        mockMvcTester
                .post()
                .uri("/api/runJob")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void testRunJobWithInvalidJobName() {
        String requestBody =
                """
                {
                    "jobName": "InvalidJob",
                    "jobId": "12345"
                }
            """;

        mockMvcTester
                .post()
                .uri("/api/runJob")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void testDeleteJob() {
        String requestBody =
                """
                    {
                        "jobName": "SampleJob",
                        "jobId": "12345"
                    }
                """;

        mockMvcTester
                .delete()
                .uri("/api/deleteJob")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON);
    }
}
