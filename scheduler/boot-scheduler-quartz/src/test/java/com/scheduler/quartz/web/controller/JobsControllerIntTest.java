package com.scheduler.quartz.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.scheduler.quartz.common.AbstractIntegrationTest;
import com.scheduler.quartz.model.common.Message;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class JobsControllerIntTest extends AbstractIntegrationTest {

    @Test
    void testGetJobs() {
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
    void testGetJobsStatuses() {
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
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(Message.class)
                .satisfies(message -> {
                    assertThat(message.getMsg()).isNull();
                    assertThat(message.isValid()).isEqualTo(true);
                    assertThat(message.getData()).isNull();
                });
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
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(Message.class)
                .satisfies(message -> {
                    assertThat(message.getMsg()).isEqualTo("CronExpression '0/5 * * * *' is invalid.");
                });
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
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(Message.class)
                .satisfies(message -> {
                    assertThat(message.getMsg()).isEqualTo("Job does not exist with key: DEFAULT.InvalidJob");
                });
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
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(Message.class)
                .satisfies(message -> {
                    System.out.println("DEBUG: message.isValid() = " + message.isValid());
                    System.out.println("DEBUG: message.getMsg() = " + message.getMsg());
                    assertThat(message.getMsg()).isEqualTo("Job does not exist with key: DEFAULT.InvalidJob");
                    assertThat(message.isValid()).isFalse();
                });
    }

    @Test
    void testDeprecatedRunJob() {
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
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(Message.class)
                .satisfies(message -> {
                    assertThat(message.getMsg())
                            .contains(
                                    "This endpoint is deprecated. Please use actuator endpoint: POST /actuator/quartz/jobs/");
                    assertThat(message.isValid()).isTrue();
                });
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

    @Test
    void testDeleteJobWithInvalidJobName() {
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
