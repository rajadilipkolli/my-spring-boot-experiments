package com.learning.shedlock.web.controllers;

import com.learning.shedlock.exception.JobNotFoundException;
import com.learning.shedlock.model.query.FindJobsQuery;
import com.learning.shedlock.model.request.JobRequest;
import com.learning.shedlock.model.response.JobResponse;
import com.learning.shedlock.model.response.PagedResult;
import com.learning.shedlock.services.JobService;
import com.learning.shedlock.utils.AppConstants;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public PagedResult<JobResponse> getAllJobs(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        FindJobsQuery findJobsQuery = new FindJobsQuery(pageNo, pageSize, sortBy, sortDir);
        return jobService.findAllJobs(findJobsQuery);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {
        return jobService.findJobById(id).map(ResponseEntity::ok).orElseThrow(() -> new JobNotFoundException(id));
    }

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@RequestBody @Validated JobRequest jobRequest) {
        JobResponse response = jobService.saveJob(jobRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/api/jobs/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(@PathVariable Long id, @RequestBody @Valid JobRequest jobRequest) {
        return ResponseEntity.ok(jobService.updateJob(id, jobRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<JobResponse> deleteJob(@PathVariable Long id) {
        return jobService
                .findJobById(id)
                .map(job -> {
                    jobService.deleteJobById(id);
                    return ResponseEntity.ok(job);
                })
                .orElseThrow(() -> new JobNotFoundException(id));
    }
}
