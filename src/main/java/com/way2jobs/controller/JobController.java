package com.way2jobs.controller;

import com.way2jobs.dto.JobCardResponse;
import com.way2jobs.dto.JobDetailResponse;
import com.way2jobs.entity.Job;
import com.way2jobs.entity.User;
import com.way2jobs.mapper.JobMapper;
import com.way2jobs.repository.SavedJobRepository;
import com.way2jobs.repository.UserRepository;
import com.way2jobs.security.JwtUtil;
import com.way2jobs.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final SavedJobRepository savedJobRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Job createJob(@RequestBody Job job) {
        return jobService.saveJob(job);
    }

    @GetMapping
    public ResponseEntity<Page<JobCardResponse>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        Page<JobCardResponse> response = jobService.getJobs(pageable)
                .map(job -> JobMapper.toJobCard(job, isJobSaved(job.getId(), authorization)));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDetailResponse> getJobById(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Job job = jobService.getJobById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));
        return ResponseEntity.ok(JobMapper.toJobDetail(job, isJobSaved(job.getId(), authorization)));
    }

    @PutMapping("/{id}")
    public Job updateJob(@PathVariable Long id,
                         @RequestBody Job job) {
        return jobService.updateJob(id, job);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
    }

    @GetMapping("/state/{stateId}")
    public ResponseEntity<Page<JobCardResponse>> getJobsByState(
            @PathVariable Long stateId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        Page<JobCardResponse> response = jobService.getJobsByState(stateId, pageable)
                .map(job -> JobMapper.toJobCard(job, isJobSaved(job.getId(), authorization)));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all-india")
    public ResponseEntity<Page<JobCardResponse>> getAllIndiaJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        Page<JobCardResponse> response = jobService.getAllIndiaJobs(pageable)
                .map(job -> JobMapper.toJobCard(job, isJobSaved(job.getId(), authorization)));
        return ResponseEntity.ok(response);
    }

    private boolean isJobSaved(Long jobId, String authorization) {
        if (jobId == null || authorization == null || authorization.isBlank()) {
            return false;
        }

        String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
        try {
            String email = jwtUtil.extractEmail(token);
            if (email == null || email.isBlank()) {
                return false;
            }

            User user = userRepository.findByEmail(email).orElse(null);
            return user != null && savedJobRepository.existsByUserAndJobId(user, jobId);
        } catch (RuntimeException ex) {
            return false;
        }
    }
}