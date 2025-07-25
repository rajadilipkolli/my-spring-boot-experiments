package com.example.bootbatchjpa.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.bootbatchjpa.common.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

@SpringBatchTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SpringBatchIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JobOperatorTestUtils jobOperatorTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private Job jobUnderTest;

    @BeforeEach
    void setup() {
        this.jobRepositoryTestUtils.removeJobExecutions();
        this.jobOperatorTestUtils.setJob(this.jobUnderTest);
    }

    @Test
    void givenReferenceOutput_whenJobExecuted_thenSuccess() throws Exception {
        // given

        // when
        JobExecution jobExecution = jobOperatorTestUtils.startJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        // then
        assertThat(actualJobInstance.getJobName()).isEqualTo("all-customers-job");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo(ExitStatus.COMPLETED.getExitCode());
        assertThat(actualJobExitStatus).isEqualTo(ExitStatus.COMPLETED);
    }

    @Test
    void myJob() throws Exception {
        // given
        JobParameters jobParameters = this.jobOperatorTestUtils.getUniqueJobParameters();

        // when
        JobExecution jobExecution = this.jobOperatorTestUtils.startJob(jobParameters);

        // then
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    }

    private JobParameters defaultJobParameters() {
        return new JobParametersBuilder()
                .addLong("minId", 0L)
                .addLong("maxId", 100L)
                .toJobParameters();
    }
}
