package com.learning.shedlock.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import com.learning.shedlock.entities.Job;
import com.learning.shedlock.mapper.JobMapper;
import com.learning.shedlock.model.response.JobResponse;
import com.learning.shedlock.repositories.JobRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private JobService jobService;

    @Test
    void findJobById() {
        // given
        given(jobRepository.findById(1L)).willReturn(Optional.of(getJob()));
        given(jobMapper.toResponse(any(Job.class))).willReturn(getJobResponse());
        // when
        Optional<JobResponse> optionalJob = jobService.findJobById(1L);
        // then
        assertThat(optionalJob).isPresent();
        JobResponse job = optionalJob.get();
        assertThat(job.id()).isOne();
        assertThat(job.text()).isEqualTo("junitTest");
    }

    @Test
    void deleteJobById() {
        // given
        willDoNothing().given(jobRepository).deleteById(1L);
        // when
        jobService.deleteJobById(1L);
        // then
        verify(jobRepository, times(1)).deleteById(1L);
    }

    private Job getJob() {
        return new Job().setId(1L).setText("junitTest");
    }

    private JobResponse getJobResponse() {
        return new JobResponse(1L, "junitTest");
    }
}
