package com.learning.shedlock.mapper;

import com.learning.shedlock.entities.Job;
import com.learning.shedlock.model.request.JobRequest;
import com.learning.shedlock.model.response.JobResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JobMapper {

    public Job toEntity(JobRequest jobRequest) {
        return new Job().setText(jobRequest.text());
    }

    public void mapJobWithRequest(Job job, JobRequest jobRequest) {
        job.setText(jobRequest.text());
    }

    public JobResponse toResponse(Job job) {
        return new JobResponse(job.getId(), job.getText());
    }

    public List<JobResponse> toResponseList(List<Job> jobList) {
        return jobList.stream().map(this::toResponse).toList();
    }
}
