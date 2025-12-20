package com.scheduler.quartz.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.scheduler.quartz.common.AbstractIntegrationTest;
import com.scheduler.quartz.model.common.Message;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class JobsControllerIntTest extends AbstractIntegrationTest {

    @Test
    void getJobs() {
        mockMvcTester
                .get()
                .uri("/api")
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(List.class);
    }

    @Test
    void getJobsStatuses() {
        mockMvcTester
                .get()
                .uri("/api/statuses")
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(List.class);
    }

    @Test
    void saveOrUpdate() {
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
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(Message.class)
                .satisfies(message -> {
                    assertThat(message.getMsg()).isNull();
                    assertThat(message.isValid()).isTrue();
                    assertThat(message.getData()).isNull();
                });
    }

    @Test
    void saveOrUpdateWithInvalidCronExpression() {
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
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(Message.class)
                .satisfies(message -> {
                    assertThat(message.getMsg()).isEqualTo("CronExpression '0/5 * * * *' is invalid.");
                });
    }

    @Test
    void pauseJob() {
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
    void pauseJobWithInvalidJobName() {
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
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(Message.class)
                .satisfies(message -> {
                    assertThat(message.getMsg()).isEqualTo("Job does not exist with key: DEFAULT.InvalidJob");
                });
    }

    @Test
    void resumeJob() {
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
    void resumeJobWithInvalidJobName() {
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
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(Message.class)
                .satisfies(message -> {
                    assertThat(message.getMsg()).isEqualTo("Job does not exist with key: DEFAULT.InvalidJob");
                });
    }

    @Test
    void deleteJob() {
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

    @Test
    void deleteJobWithInvalidJobName() {
        String requestBody =
                """
                    {
                        "jobName": "InvalidJob",
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
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(Message.class)
                .satisfies(message -> {
                    assertThat(message.getMsg()).isEqualTo("Job does not exist with key: DEFAULT.InvalidJob");
                });
    }
}
