package com.learning.shedlock.services;

import com.learning.shedlock.entities.Job;
import com.learning.shedlock.exception.JobNotFoundException;
import com.learning.shedlock.mapper.JobMapper;
import com.learning.shedlock.model.query.FindJobsQuery;
import com.learning.shedlock.model.request.JobRequest;
import com.learning.shedlock.model.response.JobResponse;
import com.learning.shedlock.model.response.PagedResult;
import com.learning.shedlock.repositories.JobRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    public JobService(JobRepository jobRepository, JobMapper jobMapper) {
        this.jobRepository = jobRepository;
        this.jobMapper = jobMapper;
    }

    public PagedResult<JobResponse> findAllJobs(FindJobsQuery findJobsQuery) {

        // create Pageable instance
        Pageable pageable = createPageable(findJobsQuery);

        Page<Job> jobsPage = jobRepository.findAll(pageable);

        List<JobResponse> jobResponseList = jobMapper.toResponseList(jobsPage.getContent());

        return new PagedResult<>(jobsPage, jobResponseList);
    }

    private Pageable createPageable(FindJobsQuery findJobsQuery) {
        int pageNo = Math.max(findJobsQuery.pageNo() - 1, 0);
        Sort sort = Sort.by(
                findJobsQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.Order.asc(findJobsQuery.sortBy())
                        : Sort.Order.desc(findJobsQuery.sortBy()));
        return PageRequest.of(pageNo, findJobsQuery.pageSize(), sort);
    }

    public Optional<JobResponse> findJobById(Long id) {
        return jobRepository.findById(id).map(jobMapper::toResponse);
    }

    @Transactional
    public JobResponse saveJob(JobRequest jobRequest) {
        Job job = jobMapper.toEntity(jobRequest);
        Job savedJob = jobRepository.save(job);
        return jobMapper.toResponse(savedJob);
    }

    @Transactional
    public JobResponse updateJob(Long id, JobRequest jobRequest) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new JobNotFoundException(id));

        // Update the job object with data from jobRequest
        jobMapper.mapJobWithRequest(job, jobRequest);

        // Save the updated job object
        Job updatedJob = jobRepository.save(job);

        return jobMapper.toResponse(updatedJob);
    }

    @Transactional
    public void deleteJobById(Long id) {
        jobRepository.deleteById(id);
    }
}
